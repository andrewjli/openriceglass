# OpenRice Glass

This is an augmented reality application for Google Glass, written primarily by
[@tomtau](https://github.com/tomtau), with input from [@andrewjli](https://github.com/andrewjli)
at the Codeaholics July 2014 Hackathon for Wearables in less than 48 hours. It
is an Augmented Reality glassware that allows users to query for nearby restaurants,
pulling data from Hong Kong's [OpenRice](http://www.openrice.com/english/),
and displays a point of interest marker for a selected restaurant in an AR overlay
to guide users to the location of the restaurant. It is based off the [Wikitude Glass SDK](http://www.wikitude.com/products/eyewear/google-glass-augmented-reality-sdk/).

This piece of glassware was built alongside [andrewjli/openricescraper](https://github.com/andrewjli/openricescraper)
for querying and parsing of OpenRice search data.

## Installation
1. `git clone https://github.com/andrewjli/openriceglass.git`
2. Import the project into ADT/Eclipse. SDKs/libraries might require some configuring.
3. Run the project as an Android Application on your Google Glass

### Dependencies
This is a piece of glassware, so obviously you need to have a Google Glass. It is
built with the Android SDK, Glass Development Kit (GDK) and the Wikitude Glass SDK.

## Usage
There are two ways to start the app after it is installed onto your Glass:

1. On the home screen, say "ok, glass" and then say "Explore nearby"; or
2. On the home screen, tap the touch pad, then tap "Explore".

Once the app is started, you are prompted to provide some search keywords. This
is optional. If you don't want to provide any, just say anything. To provide keywords,
you have to say "search" followed by your keywords. For example, "search coffee".

The app will then retrieve a list of restaurants that match your location and
keywords, and display them as a timeline of cards. You can use the touchpad to
scroll through them, and select the desired one by tapping on it.

Tapping on a card will bring up an AR view (may take a bit of time to initialise),
and will display a Point of Interest card in the direction of the restaurant so
you can find it.

To exit from the AR view or the search results, just swipe down as usual.

## Note
Currently, the location submitted to the API query is fixed for testing and demonstration
purposes. However, it is trivial to swap in real coordinates from the `LocationManager`.
