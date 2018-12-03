@Echo off
START "rmi registry" /MIN CMD /c "cd out/Ebay && rmiregistry"
START "Auction house server" /MIN CMD /c "java -cp out\Ebay;. AuctionServer.AuctionHouseServer"

REM blocks for 1s
TIMEOUT /T 1 /NOBREAK >nul

START "" /B CMD /c "buyer.bat"
START "" /B CMD /c "seller.bat"
@Echo on
