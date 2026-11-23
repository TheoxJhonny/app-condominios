const { initializeApp } = require("firebase-admin/app");
const { getAuth } = require("firebase-admin/auth");
const { getMessaging } = require("firebase-admin/messaging");
const { onValueCreated } = require("firebase-functions/v2/database");
const logger = require("firebase-functions/logger");

initializeApp();

const expiredTokenErrors = new Set([
  "messaging/invalid-registration-token",
  "messaging/registration-token-not-registered",
]);

const retryableSendErrors = new Set([
  "messaging/server-unavailable",
  "messaging/internal-error",
  "messaging/unknown-error",
  "messaging/message-rate-exceeded",
  "messaging/device-message-rate-exceeded",
  "messaging/quota-exceeded",
]);

exports.notifyAnnouncement = onValueCreated(
  {
    ref: "/communities/{communityId}/announcements/{announcementId}",
    instance: "neighborloop-b915b-default-rtdb",
    region: "us-central1",
    retry: true,
  },
  async ({ data, params }) => {
    const { communityId, announcementId } = params;
    const devices = data.ref.root.child("announcementDevices").child(communityId);
    const subscribers = (await devices.get()).val() || {};
    const uids = Object.keys(subscribers);
    const deliveries = [];

    for (let offset = 0; offset < uids.length; offset += 100) {
      const identifiers = uids.slice(offset, offset + 100).map((uid) => ({ uid }));
      const { users } = await getAuth().getUsers(identifiers);
      for (const user of users) {
        if (user.disabled || user.customClaims?.communityId !== communityId) continue;
        const installations = Object.entries(subscribers[user.uid]);
        const installationsByToken = new Map();
        for (const [installation, token] of installations) {
          const registered = installationsByToken.get(token) || [];
          registered.push(devices.child(user.uid).child(installation));
          installationsByToken.set(token, registered);
        }
        for (const [token, refs] of installationsByToken) {
          deliveries.push({
            refs,
            message: {
              token,
              data: {
                kind: "announcement",
                communityId,
                announcementId,
                recipientUid: user.uid,
              },
              android: { priority: "high", ttl: 24 * 60 * 60 * 1000 },
            },
          });
        }
      }
    }

    let failed = false;
    for (let offset = 0; offset < deliveries.length; offset += 500) {
      const batch = deliveries.slice(offset, offset + 500);
      const result = await getMessaging().sendEach(batch.map(({ message }) => message));
      for (const [index, response] of result.responses.entries()) {
        if (response.success) continue;
        const code = response.error?.code;
        const { refs, message } = batch[index];
        if (expiredTokenErrors.has(code)) {
          await Promise.all(refs.map((ref) =>
            ref.transaction((current) => current === message.token ? null : undefined)
          ));
        } else {
          logger.warn("Announcement notification failed", { announcementId, code });
          if (!code || retryableSendErrors.has(code)) failed = true;
        }
      }
    }
    if (failed) throw new Error("Some announcement notifications could not be sent.");
  }
);
