"""Assign a resident to the demo community from an authorized admin session."""

import argparse


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("uid", help="Firebase Authentication UID")
    parser.add_argument("--apply", action="store_true", help="Write the change")
    options = parser.parse_args()

    import firebase_admin
    from firebase_admin import auth

    app = firebase_admin.initialize_app(options={"projectId": "neighborloop-b915b"})
    user = auth.get_user(options.uid, app=app)
    if user.disabled:
        parser.error("La cuenta está deshabilitada.")

    claims = dict(user.custom_claims or {})
    if claims.get("communityId") not in (None, "comunidad01"):
        parser.error("La cuenta ya pertenece a otra comunidad.")
    if claims.get("admin"):
        parser.error("Esta herramienta es solo para cuentas de residente.")
    updated = {**claims, "communityId": "comunidad01", "admin": False}
    if options.apply and updated != claims:
        auth.set_custom_user_claims(user.uid, updated, app=app)
    print("Acceso aplicado." if options.apply else "Vista previa: residente de comunidad01.")


if __name__ == "__main__":
    main()
