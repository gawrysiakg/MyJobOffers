db.createUser(
    {
        user: "admin",
        pwd: "admin",
        roles: [
            {
                role: "readWrite",
                db: "myjoboffers_web"
            }
        ]
    }
)