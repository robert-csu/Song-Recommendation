# Song Recommendation application

## Input Format

Your program must accept 5 or more command-line arguments. 

`gradle run -q --args="..."`





# Example Input/Output

Note: All features from previous PAs are still expected to work.

---
command
`gradle run -q --args="'database/files/rock.csv' 'playlist.csv' '-s' '1' 'Bohemian Rhapsody'"`

rock.csv
```
Bohemian Rhapsody,user1,5
Bohemian Rhapsody,user2,4
Bohemian Rhapsody,user3,5
Stairway to Heaven,user1,5
Stairway to Heaven,user2,4
Stairway to Heaven,user3,5
Hotel California,user1,5
Hotel California,user2,4
Hotel California,user3,3
November Rain,user1,1
November Rain,user2,5
November Rain,user3,1
```

playlist.csv
```
Stairway to Heaven
Hotel California
November Rain
```

---
`gradle run -q --args="'database/files/rockpop.csv' 'rock_playlist.csv' '-s' '2' 'Bohemian Rhapsody' 'Stairway to Heaven'"`

rockpop.csv

```
Bohemian Rhapsody,rock_fan1,5
Bohemian Rhapsody,rock_fan2,4
Bohemian Rhapsody,rock_fan3,5
Bohemian Rhapsody,pop_fan1,2
Bohemian Rhapsody,pop_fan2,1
Stairway to Heaven,rock_fan1,4
Stairway to Heaven,rock_fan2,5
Stairway to Heaven,rock_fan3,4
Stairway to Heaven,pop_fan1,1
Stairway to Heaven,pop_fan2,2
Sweet Child O' Mine,rock_fan1,5
Sweet Child O' Mine,rock_fan2,4
Sweet Child O' Mine,rock_fan3,3
Sweet Child O' Mine,pop_fan1,1
Sweet Child O' Mine,pop_fan2,1
November Rain,rock_fan1,4
November Rain,rock_fan2,5
November Rain,rock_fan3,5
November Rain,pop_fan1,2
November Rain,pop_fan2,3
Hotel California,rock_fan1,3
Hotel California,rock_fan2,4
Hotel California,rock_fan3,2
Hotel California,pop_fan1,1
Hotel California,pop_fan2,1
Bad Romance,pop_fan1,5
Bad Romance,pop_fan2,4
Bad Romance,rock_fan1,1
Bad Romance,rock_fan2,2
Poker Face,pop_fan1,4
Poker Face,pop_fan2,5
Poker Face,rock_fan1,2
Poker Face,rock_fan2,1
Shallow,pop_fan1,3
Shallow,pop_fan2,4
Shallow,rock_fan1,1
Shallow,rock_fan2,1
```

rock_playlist.csv

```
November Rain
Hotel California
Sweet Child O' Mine
```

---
`gradle run -q --args="'database/files/rockpop.csv' 'mixed_playlist.csv' '-s' '2' 'Bohemian Rhapsody' 'Bad Romance'"`

rockpop.csv _(see above)_

mixed_playlist.csv

```
Shallow
Sweet Child O' Mine
Poker Face
Stairway to Heaven
November Rain
Hotel California
```
