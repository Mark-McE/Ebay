#!/bin/sh

cd out/Ebay 

rmiregistry &

cd ../..
sleep 0.5

java -cp out/Ebay:. AuctionServer.AuctionHouseServer &

sleep 0.5

gnome-terminal -e "java -cp out/Ebay:. AuctionClient.Buyer" &
gnome-terminal -e "java -cp out/Ebay:. AuctionClient.Seller"
