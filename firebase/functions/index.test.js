const assert = require("node:assert/strict");
const fs = require("node:fs");
const path = require("node:path");
const test = require("node:test");
const vm = require("node:vm");

test("selects current members, removes expired tokens, and retries temporary failures", async () => {
  const subscribers = {
    neighbor: { oldDevice: "expired", newDevice: "current" },
    moved: { device: "other-community" },
    disabled: { device: "disabled-user" },
  };
  const removed = [];
  const sent = [];
  let sendError;
  function reference(parts = []) {
    return {
      child: (key) => reference([...parts, key]),
      get: async () => ({ val: () => subscribers }),
      transaction: async (update) => {
        assert.equal(update("refreshed"), undefined);
        assert.equal(update("expired"), null);
        removed.push(parts.join("/"));
      },
    };
  }
  const modules = {
    "firebase-admin/app": { initializeApp() {} },
    "firebase-admin/auth": {
      getAuth: () => ({
        getUsers: async () => ({
          users: [
            { uid: "neighbor", customClaims: { communityId: "condo" } },
            { uid: "moved", customClaims: { communityId: "elsewhere" } },
            { uid: "disabled", disabled: true, customClaims: { communityId: "condo" } },
          ],
        }),
      }),
    },
    "firebase-admin/messaging": {
      getMessaging: () => ({
        sendEach: async (messages) => {
          sent.push(...messages);
          return {
            responses: [
              { success: false, error: { code: "messaging/registration-token-not-registered" } },
              sendError ? { success: false, error: { code: sendError } } : { success: true },
            ],
          };
        },
      }),
    },
    "firebase-functions/v2/database": {
      onValueCreated: (options, handler) => {
        assert.equal(options.instance, "neighborloop-b915b-default-rtdb");
        assert.equal(options.retry, true);
        return handler;
      },
    },
    "firebase-functions/logger": { warn() {} },
  };
  const exports = {};
  vm.runInNewContext(fs.readFileSync(path.join(__dirname, "index.js"), "utf8"), {
    exports,
    require: (name) => modules[name],
  });
  const event = {
    params: { communityId: "condo", announcementId: "notice" },
    data: { ref: { root: reference() } },
  };
  await exports.notifyAnnouncement(event);
  assert.equal(sent.map((message) => message.token).join(","), "expired,current");
  assert.deepEqual({ ...sent[1].data }, {
    kind: "announcement",
    communityId: "condo",
    announcementId: "notice",
    recipientUid: "neighbor",
  });
  assert.deepEqual(removed, ["announcementDevices/condo/neighbor/oldDevice"]);
  sendError = "messaging/invalid-argument";
  await exports.notifyAnnouncement(event);
  sendError = "messaging/server-unavailable";
  await assert.rejects(exports.notifyAnnouncement(event), /could not be sent/);
});
