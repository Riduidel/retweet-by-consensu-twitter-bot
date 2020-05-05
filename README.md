<h1 align="center">Welcome to retweet-by-consensu-twitter-bot 👋</h1>
<p>
  <img alt="Version" src="https://img.shields.io/badge/version-0.0.1--SNAPSHOT-blue.svg?cacheSeconds=2592000" />
  <a href="https://www.gnu.org/licenses/lgpl-3.0.html" target="_blank">
    <img alt="License: LGPL v3" src="https://img.shields.io/badge/License-LGPL v3-yellow.svg" />
  </a>
  <a href="https://twitter.com/Riduidel" target="_blank">
    <img alt="Twitter: Riduidel" src="https://img.shields.io/twitter/follow/Riduidel.svg?style=social" />
  </a>
</p>

> A bot allowing a group of users to collectively manage a shared account

### 🏠 [Homepage](https://github.com/Riduidel/retweet-by-consensu-twitter-bot)

#### What is that ?
Let's detail things a little ...
Suppose we are a group of friends: [@lhauspie](https://twitter.com/lhauspie), [@rbriois](https://twitter.com/rbriois) and [@Riduidel](https://twitter.com/riduidel) managing a common project for which we have a twitter account.
We have discovered that defining duty days where one of us manages that account is time-consuming, and does not allow a consistent content policy. 
So what can we do ?

1. Define a community manager ?
1. Find a way to share work in a more homogenous way

Hopefully, there are interesting theories about voting systems that can apply here.
And this bot is a support of such theories.

#### How does it works ?

We define two twitter accounts

1. **@Curator** account will be connected to this bot code for all the process of receiving requests for retweets and managing votes
1. **@Presentation** link will be used to display the validated rewteets.

Curator link will have two user lists

1. *producers* able to ask for retweets
1. *moderators*, that will validate the retweets

When someone mention the **@Curator** bot in a discussion, 
if he is either in *producers* or the *moderators* list, 
a private message is sent to each user of the *moderators* list asking for validation.
When more than a configurable given percentage of the *moderators* approve the link, it is retweeted by the **@Presentation** account.

#### OK, but technically speaking ?

We have a very comprehensive documentation about technical and architectural details.

## Author

👤 **Nicolas Delsaux**

* Website: http://riduidel.wordpress.com
* Twitter: [@Riduidel](https://twitter.com/Riduidel)
* Github: [@Riduidel](https://github.com/Riduidel)

## 🤝 Contributing

Contributions, issues and feature requests are welcome!<br />Feel free to check [issues page](https://github.com/Riduidel/retweet-by-consensu-twitter-bot/issues). 

## Show your support

Give a ⭐️ if this project helped you!

## 📝 License

Copyright © 2020 [Nicolas Delsaux](https://github.com/Riduidel).<br />
This project is [LGPL v3](https://www.gnu.org/licenses/lgpl-3.0.html) licensed.

***
_This README was generated with ❤️ by [readme-md-generator](https://github.com/kefranabg/readme-md-generator)_