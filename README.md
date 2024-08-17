# ![Unicopia](https://github-production-user-asset-6210df.s3.amazonaws.com/6429283/244022981-71021d60-e698-4ca0-88ae-bfa71be39cc6.png)

[![Build Status](https://github.com/Sollace/Unicopia/actions/workflows/gradle-build.yml/badge.svg)](https://github.com/Sollace/Unicopia/actions/workflows/gradle-build.yml)
[![Downloads](https://img.shields.io/github/downloads/Sollace/Unicopia/total.svg?color=yellowgreen)](https://github.com/Sollace/Unicopia/releases/latest)
[![Crowdin](https://badges.crowdin.net/unicopia/localized.svg)](https://crowdin.com/project/unicopia)
![](https://img.shields.io/badge/api-fabric-orange.svg)

[![es](https://img.shields.io/badge/lang-es-d52b1e.svg)](README_ES.md)
[![ru](https://img.shields.io/badge/lang-ru-d52b1e.svg)](README_RU.md)
[![cn](https://img.shields.io/badge/lang-cn-de2910.svg)](README_CN.md)

[Wiki](https://github.com/Sollace/Unicopia/wiki)

Bringing the magic of friendship to Minecraft!

What started as a humble utility to make playing as a unicorn a little more immersive has grown into a full-blown pony
conversion experience that brings new magic, mechanics and experience to the world of Minecraft to make it truly feel like you've
entered the world of Equestria!

# Features

## Learn how it feels to play as your favourite species of pony!

Unicorns, Pegasi, Earth Ponies, and even Changelings get their own special abilities
 
 - *Play as a unicorn* and learn to use magic! Craft your first spellbook and experiment, finding the different spells you can
   make and what they do, or simply delve into the lore to learn more about the past of this mysterious world!
  
   Besides casting spells, such as a shield to protect themselves, or a bolt of magic to incinerate your foes,
   Unicorns can also teleport to get around obstacles or simply reach those hard to reach places.   
 
 - *Play as a pegasus* and dominate the skies! Besides the ability to fly, pegasi can also perform sonic rainbooms, control the weather by shoving them into jars,
   and have a greater reach distance and speed than other races.
 
 - *Play as a humble background pony*! Earth Ponies are tougher and heavier than the other races. They also have the nifty ability to
   kick trees to get food and hasten the growth of crops. You'll never go hungry if you're an earth pony.

 Feeling like going over to the dark side?

 - *Become one with the hive,* and shapeshift into anything when you play as a Changeling. Hunt and feast on the love gathered from other players
   and mobs. Some forms may even come with their own unique abilities.
  
 - *Embrace the night* as a Bat Pony. Bat Ponies have unlimited screeches, can see in the dark, and can fly! Only downside is you have to wear
   really cool-cooling sunglasses otherwise the sun will burn your eyes. I think that's a fair price, don't you?
  
### Manage your diet

  Playing as a pony isn't all just kicking and zapping, though! As herbivores, your food options open up to include
  a lot of items normal players don't usually get to eat. Feeling peckish? Try for some flowers from the meadow,
  or some hay! I hear the hay burgers of good, if you can find some oats.

### Ponified Paintings
  
  Because what kind of pony mod would this be if it _didn't_ have this? Every race has at least one painting to represent them, so show your pride
  and fly that flag!
  
  Disclaimer: Rainbow flags not included (yet)

### Natural Stuff

  - Airflow is simulated (badly)

    Pegasi, beware of flying during storms! It can get dangerous out there!
    If you're playing as a flying species, or just like having nice things, try building a weather vein.

    It shows the actual, totally real and not simulated badly, wind direction of your minecraft world. Just beware
    that the direction and strength are situational (and bad), and will be different depending on where you are and
    how high up you are.

  - Hot air Rises

    No, it's not a bad Star Wars movie, it's an actual mechanic. Sand and lava will give flying species extra lift. Water does the opposite.
    Try it! Actually don't, I don't want you to drown.

### Magic Items And Artifacts
  
  - Craft and build a shrine for the Crystal Heart to provide valuable support to your friends
  - Or give out bangles of comradery to your non-unicorn buddies, so they can share in your powers,
     or just so you can laugh when you teleport and they end up coming with
  - Send and receive items using the Dragon's Breath Scroll
  - Possibly more I'm forgetting about (or am I? OoOoOooOOoo...Spooky surprise mechanics)

Have feedback on this description? Found issues, or anything missing?
DM me directly on discord.
Things are still changing, so this description may always be out of date.

# How To Play

View the HOW_TO_PLAY.md file for more details.

# Dependencies & Building

### 1.19.3 Only

This project uses reach-entity-attributes, which may not be updated at the time of this writing.
If you're building for 1.19.3, you may follow these steps to make sure it's available to git:

`git clone https://github.com/Sollace/reach-entity-attributes`

`cd reach-entity-attributes`

`gradlew build publishToMavenLocal`

### Building Unicopia

`git clone https://github.com/Sollace/Unicopia`

`cd Unicopia` 

`gradlew build`

Built jars are located in /build/bin` within the Unicopia folder after performing the above two command.

