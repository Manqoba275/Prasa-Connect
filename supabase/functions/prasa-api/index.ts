import { createClient } from "jsr:@supabase/supabase-js@2";

const corsHeaders = {
  "Access-Control-Allow-Origin": "*",
  "Access-Control-Allow-Headers": "authorization, x-client-info, apikey, content-type",
  "Access-Control-Allow-Methods": "GET, POST, PATCH, OPTIONS",
};

const json = (body: unknown, status = 200) =>
  new Response(JSON.stringify(body), {
    status,
    headers: { ...corsHeaders, "Content-Type": "application/json" },
  });

const supabase = createClient(
  Deno.env.get("SUPABASE_URL") ?? "",
  Deno.env.get("SUPABASE_SERVICE_ROLE_KEY") ?? "",
);

async function hashPassword(password: string): Promise<string> {
  const data = new TextEncoder().encode(password);
  const digest = await crypto.subtle.digest("SHA-256", data);
  return Array.from(new Uint8Array(digest)).map((b) => b.toString(16).padStart(2, "0")).join("");
}

async function readBody(req: Request) {
  try {
    return await req.json();
  } catch (_error) {
    return {};
  }
}

Deno.serve(async (req: Request) => {
  if (req.method === "OPTIONS") return new Response("ok", { headers: corsHeaders });

  const url = new URL(req.url);
  const path = url.pathname.replace(/^\/prasa-api/, "");

  try {
    if (req.method === "GET" && (path === "/" || path === "")) {
      return json({ ok: true, service: "PRASA Connect API", status: "online" });
    }

    if (req.method === "POST" && path === "/auth/register") {
      const body = await readBody(req);
      const { fullName, email, mobile, password } = body;
      if (!fullName || !email || !mobile || !password) return json({ ok: false, message: "All fields are required" }, 400);
      if (String(password).length < 6) return json({ ok: false, message: "Password must be at least 6 characters" }, 400);

      const passwordHash = await hashPassword(String(password));
      const { data, error } = await supabase
        .from("app_users")
        .insert({ full_name: fullName, email: String(email).toLowerCase(), mobile, password_hash: passwordHash })
        .select("id, full_name, email, mobile, language, notifications_enabled, offline_sync_enabled, created_at")
        .single();

      if (error) return json({ ok: false, message: error.message }, error.code === "23505" ? 409 : 500);
      return json({ ok: true, user: data }, 201);
    }

    if (req.method === "POST" && path === "/auth/login") {
      const body = await readBody(req);
      const { email, password } = body;
      if (!email || !password) return json({ ok: false, message: "Email and password are required" }, 400);

      const { data, error } = await supabase
        .from("app_users")
        .select("*")
        .eq("email", String(email).toLowerCase())
        .maybeSingle();

      if (error) return json({ ok: false, message: error.message }, 500);
      if (!data || data.password_hash !== await hashPassword(String(password))) {
        return json({ ok: false, message: "Invalid email or password" }, 401);
      }

      delete data.password_hash;
      return json({ ok: true, user: data });
    }

    if (req.method === "GET" && path === "/schedules") {
      const from = url.searchParams.get("from") ?? "";
      const to = url.searchParams.get("to") ?? "";
      let query = supabase.from("schedules").select("*").order("depart_time", { ascending: true });
      if (from) query = query.ilike("origin", `%${from}%`);
      if (to) query = query.ilike("destination", `%${to}%`);

      const { data, error } = await query;
      if (error) return json({ ok: false, message: error.message }, 500);
      return json({ ok: true, schedules: data });
    }

    if (req.method === "POST" && path === "/bookings") {
      const body = await readBody(req);
      const { userId, scheduleId } = body;
      if (!userId || !scheduleId) return json({ ok: false, message: "userId and scheduleId are required" }, 400);

      const { data: schedule, error: scheduleError } = await supabase
        .from("schedules")
        .select("*")
        .eq("id", scheduleId)
        .single();
      if (scheduleError || !schedule) return json({ ok: false, message: "Schedule not found" }, 404);

      const { data, error } = await supabase
        .from("tickets")
        .insert({
          user_id: userId,
          schedule_id: scheduleId,
          route: `${schedule.origin} -> ${schedule.destination}`,
          train: schedule.train,
          fare: 28.00,
          qr_code: `QR-${schedule.train}-${Date.now()}`,
        })
        .select("*")
        .single();

      if (error) return json({ ok: false, message: error.message }, 500);
      return json({ ok: true, ticket: data }, 201);
    }

    const ticketMatch = path.match(/^\/users\/([^/]+)\/tickets$/);
    if (req.method === "GET" && ticketMatch) {
      const { data, error } = await supabase
        .from("tickets")
        .select("*")
        .eq("user_id", ticketMatch[1])
        .order("created_at", { ascending: false });
      if (error) return json({ ok: false, message: error.message }, 500);
      return json({ ok: true, tickets: data });
    }

    if (req.method === "POST" && path === "/incidents") {
      const body = await readBody(req);
      const { userId, type, location, description } = body;
      if (!userId || !type || !location || !description) return json({ ok: false, message: "Missing incident details" }, 400);

      const { data, error } = await supabase
        .from("incidents")
        .insert({ user_id: userId, type, location, description })
        .select("*")
        .single();
      if (error) return json({ ok: false, message: error.message }, 500);
      return json({ ok: true, incident: data }, 201);
    }

    const settingsMatch = path.match(/^\/users\/([^/]+)\/settings$/);
    if (req.method === "PATCH" && settingsMatch) {
      const body = await readBody(req);
      const { language, notificationsEnabled, offlineSyncEnabled } = body;
      const { data, error } = await supabase
        .from("app_users")
        .update({ language, notifications_enabled: notificationsEnabled, offline_sync_enabled: offlineSyncEnabled })
        .eq("id", settingsMatch[1])
        .select("id, full_name, email, mobile, language, notifications_enabled, offline_sync_enabled, created_at")
        .single();
      if (error) return json({ ok: false, message: error.message }, 500);
      return json({ ok: true, user: data });
    }

    return json({ ok: false, message: "Route not found" }, 404);
  } catch (error) {
    return json({ ok: false, message: error instanceof Error ? error.message : String(error) }, 500);
  }
});
