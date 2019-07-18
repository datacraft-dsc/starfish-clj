git clone https://github.com/DEX-Company/barge.git
cd barge
git checkout dex-2019-05-24
bash -x start_ocean.sh --no-brizo --no-pleuston --no-koi --local-spree-node 2>&1 > start_ocean.log &
cd ..
sleep 240
scripts/wait_for_migration_and_extract_keeper_artifacts.sh
docker run -p 3000:3000 --env SURFER_URL=http://172.15.0.22:8080 --network="ocean_backend" --ip="172.15.0.21" canaradex/koi-clj:v0.1.1 &
