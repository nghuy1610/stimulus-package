# Stimulus Package

Stimulus Package is a Spigot plugin that gives money to players based on the number of active players and volume of
economic transactions.

## Under Development

This plugin does not currently match our proposed implementation.

## Proposed Implementation

- Configured variables:
    - `[economic interval]`: Time interval used to measure economic activity.
    - `[stimulus interval]`: Time interval used to compute stimulus payments.
    - `[payment interval]`: Time interval used to make payments to players.
    - `[desired volume]`: Desired economic activity per player over the last `[economic interval]`.
    - `[desired stimulus]`: Desired stimulus payment per player every `[stimulus interval]`.
    - `[minimum payment factor]`: Determines how much the richest player gets paid.
- Compute `[payment interval count]`: seconds in `[economic interval]` / seconds in `[payment interval]`
- Track `[active economic players]` for the past `[economic interval]`.
- Track `[active stimulus players]` for the past `[stimulus interval]`.
- A player who spends any time at all on the server during the specified time interval is considered active.
- Track the `[actual volume]` of economic transactions for past `[economic interval]`, excluding stimulus payments.
- Every `[payment interval]` do the following:
    - Determine `[active economic players]` for the past `[economic interval]`.
    - Determine `[active stimulus players]` for the past `[stimulus interval]`.
    - Determine `[actual volume]` for the past `[economic interval]`.
    - Compute `[total desired volume]`: `[desired volume]` * `[active economic players]`
    - Compute `[volume delta]`: `[total desired volume]` - `[actual volume]`
    - If `[volume delta]` is less than or equal to 0, skip the rest.
    - Compute `[stimulus factor]`: `[volume delta]` / `[total desired volume]`
    - Compute `[total stimulus]`: `[stimulus factor]` * `[desired stimulus]` * `[active stimulus players]`
    - Compute `[total payment]`: `[total stimulus]` / `[payment interval count]`
    - Assign the amount of money owned by the wealthiest active player(s) to `[highest money]`.
    - Assign the amount of money owned by the poorest active player(s) to `[lowest money]`.
    - Compute `[money delta]`: `[highest money]` - `[lowest money]`
    - Compute a `[payment factor]` for each active player:
        - If all active players have the same amount of money, or there is only one active player, use 1.
        - Otherwise:
            - Assign the amount of money owned by a player to `[player's money]`.
            - Compute `[player's offset]`: `[player's money]` - `[lowest money]`
            - Compute `[raw payment factor]`: 1 - ( `[player's offset]` / `[money delta]` )
            - Compute `[payment factor]`:
              (( 1 - `[minimum payment factor]` ) * `[raw payment factor]` ) + `[minimum payment factor]`
    - Assign the sum of `[payment factor]` for all active players to `[payment factor sum]`.
    - Compute an `[adjusted payment factor]` for each active player: `[payment factor]` / `[payment factor sum]`
    - Compute a `[payment amount]` for each active player: `[adjusted payment factor]` * `[total payment]`
    - Pay each active player their `[payment amount]`.

## Proposed Default Configuration

- `[economic interval]`: One Week - 604,800 seconds
- `[stimulus interval]`: One Day - 86,400 seconds
- `[payment interval]`: Ten Minutes - 600 seconds
- `[desired volume]`: 10,000
- `[desired stimulus]`: 1,440
- `[minimum payment factor]`: 0

With one player with no economic activity, that player would receive a payment of 10 currency every 10 minutes with a
total payment of 10,080 currency over one week.

## Contributing

Instructions for those wishing to contribute to this project are available in our
[contributing documentation](contributing.md).
