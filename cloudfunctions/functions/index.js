'use strict';

const functions = require('firebase-functions');
const {
    dialogflow,
    BasicCard,
    Button,
    SimpleResponse,
    BrowseCarousel,
    BrowseCarouselItem,
    Permission,
    Confirmation,
    MediaObject,
    Suggestions,
    NewSurface
} = require('actions-on-google');

const app = dialogflow({
    debug: true
});

const admin = require('firebase-admin');
admin.initializeApp(functions.config().firebase);

exports.dialogflowFirebaseFulfillment = functions.https.onRequest(app);

app.intent('Default Welcome Intent', (conv) => {
    conv.ask('V1.8');
});

app.intent('Temperature', (conv) => {
    return admin.database().ref('/home').once('value', (snapshot) => {
        let result = parseFloat(Math.round(snapshot.val().temperature * 100) / 100).toFixed(2);
        conv.ask(new SimpleResponse({
            speech: "Your temperature is " + result + " degrees",
            text: "Your temperature is " + result + " degrees"
        }));

    });
});

app.intent('SetLight', (conv, params) => {
    let value = params['light'];
        return admin.database().ref('/home/light').set(value).then(()=>{
            conv.ask("Setting light to " + value);
            var image = "";
            if(value === 'true'){
                image = "https://raw.githubusercontent.com/jamescoggan/AndroidThingsWorkshop/master/images/light_on.png";
            } else {
                image = "https://raw.githubusercontent.com/jamescoggan/AndroidThingsWorkshop/master/images/light_off.png";
            }
            conv.ask(new BasicCard({
            title: "Light",
            image: {
                url: image,
                accessibilityText: "Light" + value
            },
        }));
        });
});
