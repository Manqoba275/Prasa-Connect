import { createClient } from "jsr:@supabase/supabase-js@2";
import { importPKCS8, SignJWT } from "npm:jose@5.9.6";

const reply = (body: unknown, status = 200) => Response.json(body, { status });
const firebaseAudience = "https://identitytoolkit.googleapis.com/google.identity.identitytoolkit.v1.IdentityToolkit";

// Bridge the authenticated Supabase UID to Firebase Auth without duplicating passwords.
Deno.serve(async (req) => {
  if (req.method !== "POST") return reply({ message: "POST required" }, 405);
  const bearer = req.headers.get("authorization") || "";
  if (!bearer.startsWith("Bearer ")) return reply({ message: "Sign in required" }, 401);
  const auth = createClient(Deno.env.get("SUPABASE_URL")!, Deno.env.get("SUPABASE_ANON_KEY")!);
  const { data: { user }, error } = await auth.auth.getUser(bearer.slice(7));
  if (error || !user) return reply({ message: "Sign in required" }, 401);
  try {
    const raw = Deno.env.get("FCM_SERVICE_ACCOUNT_JSON");
    if (!raw) return reply({ message: "Firebase account bridge is not configured" }, 503);
    const account = JSON.parse(raw);
    if (!account.client_email || !account.private_key || !account.private_key_id)
      return reply({ message: "Firebase signing credentials are invalid" }, 503);
    const key = await importPKCS8(account.private_key, "RS256");
    const token = await new SignJWT({ uid: user.id })
      .setProtectedHeader({ alg: "RS256", kid: account.private_key_id, typ: "JWT" })
      .setIssuer(account.client_email).setSubject(account.client_email)
      .setAudience(firebaseAudience).setIssuedAt().setExpirationTime("55m")
      .setJti(crypto.randomUUID()).sign(key);
    return reply({ token });
  } catch (error) {
    console.error("Unable to issue Firebase custom token", error instanceof Error ? error.name : "unknown");
    return reply({ message: "Unable to prepare the Firebase session" }, 503);
  }
});
