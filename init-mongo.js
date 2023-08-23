db.getSiblingDB("admin").createUser(
    {
        user: "admin",
        pwd: "admin",
        roles: [
            {
                role: "readWrite",
                db: "offers"
            },
            {
                role: "dbOwner",
                db: "offers"
            }
        ]
    }
)