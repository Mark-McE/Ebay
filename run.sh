#!/bin/sh

cd out/Ebay 

rmiregistry &

sleep 0.5

java AuctionServer.AuctionHouseServer &

sleep 0.5

gnome-terminal -e "java AuctionClient.Buyer" &
gnome-terminal -e "java AuctionClient.Seller"
