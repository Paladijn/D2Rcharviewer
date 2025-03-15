# Diablo.run API connection
endpoint:  
```
POST https://api.diablo.run/sync
Content-Type: application/json
``` 

Payload description: https://github.com/DiabloRun/diablorun-api-server/blob/master/src/sync/payload.ts  
Fields set by client in https://github.com/DiabloRun/diablorun-d2r-client/blob/main/src/DI/payload.ts#L27  
Derived from d2s javascript: https://github.com/dschu012/d2s - item properties at https://github.com/dschu012/d2s/blob/master/src/d2/attribute_enhancer.ts

Token through https://diablo.run/setup - APIKEY remains identical between sign-out/in, so don't share it publicly

## Improvements
* set resistance values
* set fcr/fhr/etc.
* correct values for hp/mana
* Correct values for attributes (str/dex/etc. with bonuses)
* Name Runewords and include their stats (Stealth is now TalEth in name)
* hireling stats
* cube/inventory/stash as well (javascript client only sends equipped items, perhaps skip the pots/tomes/scrolls/gems)
* filter on charactername (so we don't upload mule characters)
