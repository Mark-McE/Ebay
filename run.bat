@Echo off
START "rmi registry" /MIN CMD /c "cd out/Ebay && rmiregistry"
START "Auction house server" /MIN CMD /k "java -cp out\Ebay;lib\jgroups-3.6.14.Final.jar;. AuctionServer.AuctionHouseServer"

REM blocks for 1s
TIMEOUT /T 1 /NOBREAK >nul

REM START "" /B CMD /c "buyer.bat"
REM START "" /B CMD /c "seller.bat"
@Echo on
