import { createClient } from "jsr:@supabase/supabase-js@2";
import { GoogleAuth } from "npm:google-auth-library@9";

const reply = (body: unknown, status = 200) => Response.json(body, { status });
// Only explicitly allowlisted, authenticated administrators can send generic service alerts.
// Never send passenger details: a device token can survive an offline logout.
Deno.serve(async (req) => {
  if (req.method !== "POST") return reply({ message: "POST required" }, 405);
  const bearer = req.headers.get("authorization") || "";
  if (!bearer.startsWith("Bearer ")) return reply({ message: "Sign in required" }, 401);
  const url = Deno.env.get("SUPABASE_URL")!;
  const auth = createClient(url, Deno.env.get("SUPABASE_ANON_KEY")!);
  const { data: { user }, error } = await auth.auth.getUser(bearer.slice(7));
  if (error || !user) return reply({ message: "Sign in required" }, 401);
  const admins = (Deno.env.get("ALERT_ADMIN_USER_IDS") || "").split(",").map(v => v.trim());
  if (!admins.includes(user.id)) return reply({ message: "Forbidden" }, 403);
  try {
    const body = await req.json();
    if (typeof body.title !== "string" || typeof body.message !== "string" ||
        !body.title.trim() || !body.message.trim() || body.title.length > 120 || body.message.length > 1000)
      return reply({ message: "Invalid alert" }, 400);
    const secret = Deno.env.get("FCM_SERVICE_ACCOUNT_JSON");
    if (!secret) return reply({ message: "Push delivery has not been configured" }, 503);
    const credentials = JSON.parse(secret);
    const google = new GoogleAuth({ credentials, scopes: ["https://www.googleapis.com/auth/firebase.messaging"] });
    const accessToken = await google.getAccessToken();
    const db = createClient(url, Deno.env.get("SUPABASE_SERVICE_ROLE_KEY")!);
    const saved = await db.from("service_alerts").insert({ title: body.title, message: body.message }).select("id").single();
    if (saved.error) throw saved.error;
    let sent = 0, failed = 0, offset = 0;
    const seen = new Set<string>();
    // Paginate to avoid silently stopping at the database's default response limit.
    while (true) {
      const devices = await db.from("device_tokens").select("user_id,token").order("user_id").order("token").range(offset,offset+199);
      if (devices.error) throw devices.error;
      if (!devices.data.length) break;
      const preferences = await db.from("profiles").select("id,notifications_enabled").in("id",devices.data.map(d => d.user_id));
      if (preferences.error) throw preferences.error;
      const disabled = new Set(preferences.data.filter(p => !p.notifications_enabled).map(p => p.id));
      for (const device of devices.data) {
        if (disabled.has(device.user_id) || seen.has(device.token)) continue;
        seen.add(device.token);
        const response = await fetch(`https://fcm.googleapis.com/v1/projects/${credentials.project_id}/messages:send`, {
          method: "POST", headers: { Authorization: `Bearer ${accessToken}`, "Content-Type": "application/json" },
          body: JSON.stringify({ message: { token: device.token, data: { title: body.title, message: body.message, alertId: saved.data.id }, android: { priority: "high" } } }),
        });
        if (response.ok) sent++; else failed++;
        await response.arrayBuffer();
      }
      offset += devices.data.length;
    }
    return reply({ id: saved.data.id, sent, failed });
  } catch {
    return reply({ message: "Alert delivery failed; inspect the alerts table before retrying to avoid duplicates." }, 500);
  }
});
