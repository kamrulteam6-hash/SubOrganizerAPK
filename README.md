# SubOrganizer — Android

Native Kotlin + Jetpack Compose app, same Supabase project/database as the web app
(`suborganizer-next`) and the browser extension. Package: `com.suborganizer.android`.

## Opening the project

1. Open this folder (`suborganizer-android`) directly in **Android Studio** (Meerkat or
   newer — needs AGP 9.2 / Gradle 9.4.1 support).
2. Android Studio will offer to generate the Gradle wrapper jar automatically on first
   sync (the wrapper `.properties` file is committed, but the binary `gradle-wrapper.jar`
   is not — that's normal, Studio handles it). If it doesn't prompt, run
   `gradle wrapper --gradle-version 9.4.1` once with a local Gradle install.
3. Sync Gradle. First sync will download dependencies — give it a few minutes.
4. Run on a device or emulator running **API 26+** (Android 8.0 Oreo or newer — required
   for `NotificationListenerService` reliability).

## What's built (v1 scope)

- **Auth** — same Supabase Auth as the web app (login/signup, same users)
- **Dashboard** — monthly total, trial radar, upcoming renewals
- **My Subscriptions** — searchable list, detail view, cancel/delete
- **Add** — manual entry
- **Review** — the smart-collection queue (see below) — approve or discard
- **Settings** — sign out, permission status/toggles for the two collection channels
- **Renewal reminders** — WorkManager job every 6h + boot receiver, local notifications

**Not built in v1** (flagged, not forgotten): Calendar, Analytics, Family, Admin. The
architecture (repositories, Supabase client, theme) is there to add them the same way
the web app has them — happy to build these next if you want full parity.

## Smart collection — how it actually works

Two on-device channels feed the same local **Review queue** (`DraftRepository`, backed
by DataStore — nothing here touches Supabase until you tap Approve):

1. **Notification access** (`NotificationCollectorService`) — reads notification
   title/text from other apps (banking apps, Gmail/Outlook previews, Google Pay, Play
   Billing receipts). This is the **Play-Store-compliant** channel — it uses the special
   "Notification access" toggle, not a restricted permission declaration.
2. **SMS** (`SmsReceiver`) — reads incoming SMS bodies for bank "you were charged"
   texts. **Read this before you publish to Play**:

### ⚠️ SMS permission & Google Play

`READ_SMS`/`RECEIVE_SMS` are in the "restricted permissions" bucket on Google Play.
Google generally only approves them for apps that are the user's **default SMS handler**
or fall into a narrow list of approved categories (a subscription tracker doesn't
qualify). Realistic options, in order of how "real" they are:

- **Ship without SMS on Play** (notification access alone still catches most banking
  apps' own notifications) — safest, and what I'd actually recommend for a Play listing.
  Gate the SMS permission request behind a build flavor or remote flag so a Play build
  simply never asks for it.
- **Distribute outside Play** (direct APK / internal testing / enterprise) — SMS works
  fine there, no policy review.
- **Apply for the Permissions Declaration Form** on Play Console and make the case —
  Google does grant this sometimes for finance apps, but it's a real review process, not
  guaranteed, and adds launch risk/delay.

The code supports all three paths already (permission request is a single toggle in
Settings, not baked into the app's critical path) — this is a business decision for you
to make when you're closer to actually publishing, not something to solve in code today.

Both collectors run `DetectionEngine` (`detection/DetectionEngine.kt`) — the same
confidence-scoring approach as the browser extension's `content.js`, adapted for short
text instead of full page content.

## Things worth double-checking on first build

I verified the Supabase Kotlin SDK's exact API (package names, `createSupabaseClient`,
`auth.signInWith(Email)`, `from("table").select/insert/update`) against the live docs
while writing this, since guessing wrong here would just fail to compile — but I could
not actually run a Gradle build in my environment. If anything doesn't resolve:

- Check the [Supabase Kotlin reference](https://supabase.com/docs/reference/kotlin/introduction)
  for the exact method signature — the SDK evolves.
- Dependency versions in `gradle/libs.versions.toml` were current as of this session;
  Android Studio will flag any that have since moved (safe to accept its suggested bumps).
- `AuthRepository.sessionStatus` (a reactive `StateFlow`) is wired up but **not yet used**
  for navigation — `MainActivity` currently just checks `currentUserId` once at launch.
  If a session expires mid-use, the app won't notice until restarted. Worth wiring
  `sessionStatus` into navigation as a follow-up.

## Branding

Same palette as the web app: `#06070C` background, indigo (`#6366F1`) → fuchsia
(`#D946EF`) gradient accents, per-category colors in `ui/theme/Color.kt` mirroring
`suborganizer-next/src/lib/format.ts`. The app icon is generated from the same "S" mark.
