# Vivy

My mission is to make everyone happy by singing.

## Let me sing?
Rename `example.env` to `.env` and put your discord vivy token. Do not forget to set the shard id 
and the total number of shards you want to deploy.

NOTE: Sharding is assumed to be a single instance of a machine, hence the need to provide the shard id.

```bash
./gradlew installDist
docker-compose build
docker-compose up
```

An example [systemd unit file](systemd/vivy.service) is included in the repository.