import http from 'k6/http';

const apiUrl = 'http://localhost:9780';

export let options = {
    vus: 50,
    iterations: 1000,
    duration: '10m',
};

export default function () {
    const orderPayload = JSON.stringify({
        orderItems: [
            { productId: 1, quantity: 1 },
        ]
    });

    const orderResponse = http.post(`${apiUrl}/order`, orderPayload, {
        headers: {
            'Content-Type': 'application/json',
            'accept': '*/*',
        },
    });

    if(orderResponse.status !== 200){
        console.log("주문 실패 응답:", orderResponse.status, orderResponse.body);
    }

    const orderId = orderResponse.json('orderId');
    const placePayload = JSON.stringify({
        orderId: orderId
    });

    const params = {
        headers: {
            'Content-Type': 'application/json',
            'accept': '*/*',
        },
    };

    const batchRequests = Array.from({ length: 3 }, () => ({
        method: 'POST',
        url: `${apiUrl}/order/place`,
        body: placePayload,
        params: params
    }));

    const responses = http.batch(batchRequests);

    responses.forEach((response, index) => {
        if(response.status !== 200){
            console.log(`Request ${index + 1} failed with status: ${response.status}, body: ${response.body}`);
        }
    });
}
