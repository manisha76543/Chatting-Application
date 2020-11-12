//const functions = require('firebase-functions');

// // Create and Deploy Your First Cloud Functions
// // https://firebase.google.com/docs/functions/write-firebase-functions
//
// exports.helloWorld = functions.https.onRequest((request, response) => {
//   functions.logger.info("Hello logs!", {structuredData: true});
//   response.send("Hello from Firebase!");
// });


'use strict'

const functions = require('firebase-functions');
var admin = require("firebase-admin");

admin.initializeApp(functions.config().firebase);

exports.sendNotification = functions.database.ref('/Notifications/{user_id}/{notification}').onWrite((change, context) =>{

		const user_id = context.params.user_id;
		const notification_id = context.params.notification;
		//console.log('The user id is : ',user_id);
		
const notification_options = {
    priority: "high",
    timeToLive: 60 * 60 * 24
  };

		console.log('we have notification to send to : ', user_id);

if(!change.after.val())
{
	return console.log('A Notification has been deleted from the database :', notification_id);	
}

//------------------------------------------------------------------------


const fromUser = admin.database().ref(`/Notifications/${user_id}/${notification_id}`).once('value');
return fromUser.then(fromUserResult => {

	const from_user_id = fromUserResult.val().from;
	console.log('You have new Notification from : ', from_user_id);

	const userQuery = admin.database().ref(`Users/${from_user_id}/name`).once('value');
	const deviceToken = admin.database().ref(`/Users/${user_id}/device_token`).once('value');

	return Promise.all([userQuery, deviceToken]).then(result =>{

			const userName = result[0].val();
			const token_id = result[1].val();



			const option = notification_options;
	const payload={
	notification:{

		title : "Friend Request",
		body: `${userName} has sent you request`,
		icon: "default",
		click_action : "com.example.lapitchat_TARGET_NOTIFICATION"
},

	data : {
		from_user_id : from_user_id
	}

};
 return admin.messaging().sendToDevice(token_id, payload,option).then(response=>{
      console.log('This was the notification Feature');
      return true;
    },err=>
    {
      throw err;
    });




	});
	



});





	});


//});//itself

//});//last wala


//------------------------------------------------------------------------

/*

const deviceToken = admin.database().ref('/Users/'+user_id+'/device_token').once('value');
return deviceToken.then(result=>{

	const token_id = result.val();
	console.log("token_id ",token_id);

	const options =  notification_options;
	const payload={
	notification:{
		title : "Friend Request",
		body: "You have received a new Friend Request",
		icon: "default"
}

};
return admin.messaging().sendToDevice(token_id, payload,options).then(response => {
    return console.log("Successfully sent message:", response);
   
  });
  
});


});

*/