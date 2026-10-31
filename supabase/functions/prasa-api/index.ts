import { createClient } from "jsr:@supabase/supabase-js@2";
const headers = { "Content-Type": "application/json", "Access-Control-Allow-Origin": "*",
  "Access-Control-Allow-Headers": "authorization, apikey, content-type", "Access-Control-Allow-Methods": "GET,POST,PATCH,DELETE,OPTIONS" };
const reply = (body: unknown, status = 200) => new Response(JSON.stringify(body), { status, headers });

// User-scoped clients enforce RLS. Never trust userId from a request body.
// Reference: https://supabase.com/docs/guides/functions/auth
Deno.serve(async (req) => {
  if (req.method === "OPTIONS") return new Response(null, { headers });
  const url = new URL(req.url), path = url.pathname.split("prasa-api")[1] || "/";
  if (path === "/" && req.method === "GET") return reply({ service: "PRASA Connect", version: 2 });
  const authorization = req.headers.get("Authorization") || "";
  if (!authorization.startsWith("Bearer ")) return reply({ message: "Sign in required" }, 401);
  const db = createClient(Deno.env.get("SUPABASE_URL")!, Deno.env.get("SUPABASE_ANON_KEY")!,
    { global: { headers: { Authorization: authorization } }, auth: { persistSession: false } });
  const { data: { user }, error: authError } = await db.auth.getUser(authorization.slice(7));
  if (authError || !user) return reply({ message: "Session expired. Sign in again." }, 401);
  try {
    let result;
    if (req.method === "GET" && path === "/schedules") {
      let query = db.from("schedules").select("*").order("depart_time");
      const from = url.searchParams.get("from"), to = url.searchParams.get("to");
      if (from) query = query.ilike("origin", from.replace(/[%_]/g, ""));
      if (to) query = query.ilike("destination", to.replace(/[%_]/g, ""));
      result = await query;
    } else if (req.method === "GET" && path === "/tickets") {
      result = await db.from("bookings").select("*").order("created_at", { ascending: false });
    } else if (req.method === "POST" && path === "/bookings") {
      const body = await req.json();
      result = await db.rpc("book_journey", { p_schedule: body.scheduleId, p_request: body.requestId });
    } else if (req.method === "POST" && path === "/incidents") {
      const body = await req.json();
      result = await db.from("reports").insert({ id: body.id, user_id: user.id,
        type: body.type, location: body.location, description: body.description }).select().single();
      if (result.error?.code === "23505") result = await db.from("reports").select().eq("id", body.id).single();
    } else if (req.method === "GET" && path === "/incidents") {
      result = await db.from("reports").select().order("created_at", { ascending: false });
    } else if (req.method === "GET" && path === "/alerts") {
      result = await db.from("service_alerts").select().order("created_at", { ascending: false });
    } else if (req.method === "POST" && path === "/devices") {
      const body = await req.json();
      if (typeof body.token !== "string" || body.token.length < 20 || body.token.length > 4096)
        return reply({ message: "Invalid device token" }, 400);
      result = await db.from("device_tokens").upsert({ user_id: user.id, token: body.token });
    } else if (req.method === "DELETE" && path === "/devices") {
      result = await db.from("device_tokens").delete().eq("user_id", user.id);
    } else if (req.method === "GET" && path === "/profile") {
      result = await db.from("profiles").select().eq("id", user.id).maybeSingle();
    } else if (req.method === "PATCH" && path === "/profile") {
      const b = await req.json();
      if (!["en", "zu", "af"].includes(b.language) || typeof b.notifications_enabled !== "boolean" ||
          typeof b.offline_sync_enabled !== "boolean") return reply({ message: "Invalid settings" }, 400);
      result = await db.from("profiles").upsert({ id: user.id, full_name: String(b.full_name || "").slice(0,120),
        mobile: String(b.mobile || "").slice(0,30), language: b.language,
        notifications_enabled: b.notifications_enabled, offline_sync_enabled: b.offline_sync_enabled }).select().single();
    } else return reply({ message: "Route not found" }, 404);
    if (result.error) {
      console.error("Database request failed", result.error.code);
      return reply({ message: "Unable to complete request. Check your input and retry." }, 400);
    }
    return reply({ data: result.data });
  } catch {
    return reply({ message: "Invalid request or temporarily unavailable service" }, 400);
  }
});
