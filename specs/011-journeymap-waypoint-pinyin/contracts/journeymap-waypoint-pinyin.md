# Contract: JourneyMap waypoint pinyin filter

## Match API（模组内）

```text
WaypointNameMatch.matches(query, name) -> boolean

GIVEN query blank → true
GIVEN name contains query (case-insensitive) → true
GIVEN pinyin full/initials contains query → true
ELSE false
```

## JourneyMap UI

```text
GIVEN JourneyMap waypoint manager search filter runs
WHEN comparing a waypoint name to the search box text
THEN retain waypoint if JM native match OR WaypointNameMatch.matches
```

## Soft dependency

```text
GIVEN journeymap mod absent
THEN mcbilingual-journeymap mixins not applied / no ClassNotFound
AND client reaches world
```

## Non-goals

- Persisting pinyin on waypoints  
- Showing pinyin in the list label  
- Group-name pinyin (unless same filter function covers it incidentally)
