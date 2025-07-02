# PokeBinder

PokeBinder is an application built on AndroidStudio that grabs pokemon card sets from a locally hosted database that contains all sets of English 
pokemon cards as well as a "Gigadex" which is an unofficial set of Pokemon cards containing one of every pokemon. This application allows users
to keep track of which cards they have, their prices, what cards they still need and which cards they are specifically looking for.

## Features

*   Set Selection: Displays every currently released set + a "Gigadex" set for users to quickly and easily choose the sets they want to update.
<img src="app/src/main/res/drawable/poke_set.png" alt="Set Image" width="200"/>

*   Gigadex: A set containing one of every Pokemon, cards you own will display, cards you dont will show the back of a card. Clicking on a card will
display every currently released card of that Pokemon allowing the user to choose their owned card.
<div style="display: flex; gap: 20px;">
    <img src="app/src/main/res/drawable/poke_gigadex.png" alt="Gigadex Image" width="200"/>
    <img src="app/src/main/res/drawable/poke_gigaselect.png" alt="Gigadex Select Image" width="200"/>
</div>

*   Search: When in vertical mode in the binder, you can search for certain Pokemon using the searchbar at the top, which filters which Pokemon are shown.
<img src="app/src/main/res/drawable/poke_search.png" alt="Search Image" width="200"/>

*   Set Select: For sets that aren't the Gigadex, gray Pokeballs will appear on the bottom of the card, the user clicks on this when they own the card
which turns it red and saves that the user owns these cards in the database.
<img src="app/src/main/res/drawable/poke_set_cards.png" alt="Set Select Image" width="200"/>

*   Card Info: When the image of the card is pressed, it opens a larger image with some card information which includes its name and price.
<img src="app/src/main/res/drawable/poke_card_info.png" alt="Card Info Image" width="200"/>

## Other Notes
The database is run using SQLite and is required for the project to work.
config.xml and private.xml files were not published because they contain sensitive data that is also required for the project to run.
