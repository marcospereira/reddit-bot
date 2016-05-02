# Facebook Reddit Bot

This is a reddit bot for Facebook Messenger. It enables you to list news from your favorite subreddit.

It uses [Reddit API](https://www.reddit.com/dev/api) and Facebook's [Messenger Platform](https://developers.facebook.com/docs/messenger-platform) to create a simple bot that list submissions in a subreddit.

> Keep in mind that this is just an experiment to play with Messenger Platform.

## How it works

First, get familiar with Messenger Platform. The following documentation pages are enough to get context of how the platform works:

1. [Getting started](https://developers.facebook.com/docs/messenger-platform/quickstart)
2. [Complete guide](https://developers.facebook.com/docs/messenger-platform/implementation)

Then start by reading `controllers.MessengerController.receiveMessage()` action, which [receives a message](https://developers.facebook.com/docs/messenger-platform/implementation#receive_message), parse it to a `models.PostedMessage` and decide how to respond. The response could be either a [simple text message](https://developers.facebook.com/docs/messenger-platform/send-api-reference#request) or a bubble message with cards (see the [button template](https://developers.facebook.com/docs/messenger-platform/send-api-reference#button_template)).

Reddit-bot tries to parse any request message's text with the pattern `/subreddit/order` and then, queries Reddit API to return the submissions according to the requested order. This response is sent as button messages. If the request message's text does not match the pattern, it will just return a help message. This all happens at `controllers.MessengerController.receiveMessage()` action.

## Tips while developing a Messenger Bot

1. Check the responses (including the headers) of requests that you are making to Facebook. Most of the errors are very clear and well documented.
2. Have a way to reproduce the requests you are making. Play WS offers an [utility](https://www.playframework.com/documentation/2.5.x/ScalaWS#Request-Filters) to log requests as `curl` and you can then change it and test variations faster.

## Let's Encrypt and Heroku

Facebook requires that your callback URLs uses HTTPS. Fortunately, [Let's Encrypt](https://letsencrypt.org/) exists and you can use it, instead of suffering all the pain involved in turn on and manage HTTPS.

Must reads:

1. [Let's Encrypt: Getting Started](https://letsencrypt.org/getting-started/)
2. [Heroku: SSL Endpoint](https://devcenter.heroku.com/articles/ssl-endpoint)

Requirements:

1. [Let's Encrypt Client](https://github.com/letsencrypt/letsencrypt)
2. [Heroku Toolbelt](https://toolbelt.heroku.com/)

Currently, there is no wrapper/add-on that integrates Let's Encrypt and Heroku, so you need to do the setup manually. Considering that you have both Let's Encrypt Client and Heroku Toolbelt installed, run the following commands:

```bash
./letsencrypt-auto --test-cert certonly --manual -d <your-app-name>.herokuapp.com
```

This will print something like this:

```
Make sure your web server displays the following content at
http://<your-app-name>.herokuapp.com/.well-known/acme-challenge/<some-key-here> before continuing:

<some-bigger-key-here>
```

Before continuing, you need to set the suggested value to your Heroku Reddit Bot deploy:

```bash
heroku config:set LETS_ENCRYPT_VALUE="<some-bigger-key-here>"
```

Where `<some-bigger-key-here>` is the value printed by `letsencrpyt-auto` command. Your app will then restart and you can continue:


```bash
heroku addons:create ssl:endpoint
sudo heroku certs:add /etc/letsencrypt/live/<your-app-name>.herokuapp.com/cert.pem /etc/letsencrypt/live/<your-app-name>.herokuapp.com/privkey.pem
```

If successful, Heroku will reply with something like:

```
Resolving trust chain... done
Adding SSL Endpoint to the-reddit-bot... done
the-reddit-bot now served by kagawa-95924.herokussl.com
Certificate details:
Common Name(s): the-reddit-bot.herokuapp.com
Expires At:     2016-07-18 19:59 UTC
Issuer:         /C=US/O=Let's Encrypt/CN=Let's Encrypt Authority X3
Starts At:      2016-04-19 19:59 UTC
Subject:        /CN=the-reddit-bot.herokuapp.com
SSL certificate is verified by a root authority.
```