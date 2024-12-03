# MACC_project

Days:

- Wednesday: morning or afternoon/evening;
- Thursday: morning or afternoon/evening;

- Saturday or Sunday (depending on the time and need).

RadarPhone

- Radar game with two phones (one player searches and the other one hides it). Every x seconds or every minute the position of the hiddenphone is pinged to the radar UI of the main player and he has to try to reach it.
- Once the player reaches the hidden phone he has to take a photo to complete the game.
- Possibility to customize UI colors, nicknames, light and dark mode, etc.
- Take inspiration from standard or more advancved radar images on internet.
- Ai -> algorithm in pythonanywhere that has to recognize and validate the photo of the smartphone found at the end of the game
- Check if he is close and if it is and sends the photo from his cell phone, so we end the game.
- Sql database.
- time constraint of y minutes to win the game, and at least z minutes have to pass to win the game.
- Check how we can implement "online" feature (so how the two people playing communicate with each other, if through a server or P2P)
- balsamiq for mockups.
  
![](./pics/example1.png)
![](./pics/example2.png)

## SCREENS
- The screen we see at the beginning is a screen with a text "press anywhere to start"
- Once we have pressed the screen, the menu to register opens, asking us for the user, email and password. To register, we must press the "register" button. If we are already registered, we can press the "are registered, press here" button, which will open the menu to enter our email and password to enter.
- Once we have entered the game, we have to enter the password to be able to connect with the other player, once we have done so, it will let us choose between the two game modes. Then we are asked if we want to play as a seeker or as a holder. We also have some settings that allow us to change the theme and the rules of the game. (To make the connection between the two users, the user searching and the one who holds the mobile phone, send the same code to the server. If the server sees that it is the same code, it matches them and the game starts)
- The customization screen allows us to change the username, we can also change the volume, and switch between different themes, we have the light theme and the dark theme.
- If we click on the button to know the rules, a new screen will open with the text.
- Once we have the connection made:
  - If we are the seeker, we will see the search radar on the screen. Our position will appear in the center and we will have to find the player who is holding the phone. We will also see a counter that will tell us how much time we have left before the game ends. We have a camera button which we will use when we find the holder. And also, we have a button to leave the game.
  - If we are the hider, we will see a text that tells us that the game has started and that the seeker is trying to find us, and also a counter with the remaining game time. And also, we have a button to leave the game. A pop-up will appear when the seeker has won or lost the game.
