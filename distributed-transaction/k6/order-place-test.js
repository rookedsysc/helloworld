import http from 'k6/http';

const apiUrl = 'http://localhost:9780';

export let options = {
    vus: 100,
    duration: '10m',
};

export default function () {
    const orderPayload = JSON.stringify({
        orderItems: [
            { productId: 1, quantity: 2 },
        ]
    });

    const orderResponse = http.post(`${apiUrl}/order`, orderPayload, {
        headers: {
            'Content-Type': 'application/json',
            'accept': '*/*',
        },
    });

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

    const batchRequests = Array.from({ length: 10 }, () => ({
        method: 'POST',
        url: `${apiUrl}/order/place`,
        body: placePayload,
        params: params
    }));

    const responses = http.batch(batchRequests);

    responses.forEach((response, index) => {
        console.log(`Request ${index + 1} status: ${response.status}`);
    });
}
