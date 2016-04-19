# Facebook Reddit Bot

This is a reddit bot for Facebook Messenger. It enables you to list news from your favorite subreddit.

## Let's Encrypt and Heroku

Facebook requires that your callback URLs uses HTTPS. Fortunately, [Let's Encrypt](https://letsencrypt.org/) exists and you can use them, instead of suffering all the pain involved in turn on and manage HTTPS.

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

Where `<some-bigger-key-here>` is the value printed by `letsencrpyt-auto` command. Your app will then restart and you can continue.

