// Added admin Cloud Functions for approving users, toggling admin, and requesting password reset

Parse.Cloud.define("adminApproveUser", async (request) => {
  const { objectId, isApproved } = request.params;
  if (!objectId) {
    throw new Parse.Error(Parse.Error.INVALID_PARAMETER, "objectId is required");
  }
  try {
    const query = new Parse.Query(Parse.User);
    const user = await query.get(objectId, { useMasterKey: true });
    user.set("isApproved", !!isApproved);
    await user.save(null, { useMasterKey: true });
    return { success: true, objectId: user.id };
  } catch (err) {
    console.error("adminApproveUser error:", err);
    throw new Parse.Error(Parse.Error.INTERNAL_SERVER_ERROR, err.message || String(err));
  }
});

Parse.Cloud.define("adminToggleAdmin", async (request) => {
  const { objectId, isAdmin } = request.params;
  if (!objectId) {
    throw new Parse.Error(Parse.Error.INVALID_PARAMETER, "objectId is required");
  }
  try {
    const query = new Parse.Query(Parse.User);
    const user = await query.get(objectId, { useMasterKey: true });
    user.set("isAdmin", !!isAdmin);
    await user.save(null, { useMasterKey: true });
    return { success: true, objectId: user.id };
  } catch (err) {
    console.error("adminToggleAdmin error:", err);
    throw new Parse.Error(Parse.Error.INTERNAL_SERVER_ERROR, err.message || String(err));
  }
});

// Server-side password reset helper that triggers Parse's requestPasswordReset
Parse.Cloud.define("adminRequestPasswordReset", async (request) => {
  const { email } = request.params;
  if (!email) {
    throw new Parse.Error(Parse.Error.INVALID_PARAMETER, "email is required");
  }
  try {
    // Parse.User.requestPasswordReset is allowed on server side
    await Parse.User.requestPasswordReset(email);
    return { success: true, message: `Reset requested for ${email}` };
  } catch (err) {
    console.error("adminRequestPasswordReset error:", err);
    throw new Parse.Error(Parse.Error.INTERNAL_SERVER_ERROR, err.message || String(err));
  }
});
