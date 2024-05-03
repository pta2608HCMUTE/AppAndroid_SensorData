const functions = require('firebase-functions');
const admin = require('firebase-admin');
admin.initializeApp();

exports.sendNotification = functions.database.ref('/sensorData/BED ROOM').onWrite(async (change, context) => {
    const data = change.after.val();
    const thresholds = data;
    const doAm = data.doAm;
    const nhietDo = data.nhietDo;
    const doAmSetup = thresholds.doAmSetup;
    const nhietDoSetup = thresholds.nhietDoSetup;

    // Tạo một payload chung
    let payload = {
        notification: {
            title: 'Cảnh báo!',
            body: '',
        },
    };

    // Kiểm tra điều kiện
    if (doAm > doAmSetup) {
        payload.notification.body += `Độ ẩm vượt quá giới hạn: Độ ẩm: ${doAm}\n`;
    }

    if (nhietDo > nhietDoSetup) {
        payload.notification.body += `Nhiệt độ vượt quá giới hạn: Nhiệt độ: ${nhietDo}\n`;
    }

    // Nếu có thông báo cần gửi
    if (payload.notification.body !== '') {
        // Lấy danh sách registration tokens (FMC tokens) từ Firebase Database
        const registrationTokens = ['registration_token_1', 'registration_token_2']; // Thay thế bằng danh sách tokens của các thiết bị

        // Gửi thông báo sử dụng FCM
        const response = await admin.messaging().sendToDevice(registrationTokens, payload);

        // Xử lý kết quả
        response.results.forEach((result, index) => {
            const error = result.error;
            if (error) {
                console.error('Error sending notification:', error);
            } else {
                console.log('Notification sent successfully to', registrationTokens[index]);
            }
        });
    }

    return null;
});
