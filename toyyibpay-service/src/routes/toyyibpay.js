import express from 'express';

const router = express.Router();

router.post('/callback', (req, res) => {
  const payload = req.body;

  console.log('ToyyibPay callback received:', payload);

  // TODO: persist payload details to database or queue for processing.

  res.json({ status: 'received' });
});

router.post('/simulate', (req, res) => {
  const samplePayload = {
    billcode: req.body.billcode ?? 'DEBUG123',
    order_id: req.body.order_id ?? 'ORDER-001',
    status: req.body.status ?? '1',
    msg: req.body.msg ?? 'Payment successful',
    amount: req.body.amount ?? '1000'
  };

  console.log('ToyyibPay simulated payload:', samplePayload);

  res.json({ status: 'simulated', payload: samplePayload });
});

export default router;

