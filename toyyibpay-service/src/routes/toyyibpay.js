import express from 'express';
import { createBill, getBillTransactions } from '../services/toyyibpay.js';

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

router.post('/payment/bill', async (req, res) => {
  try {
    const result = await createBill(req.body || {});
    res.json({
      billCode: result.billCode,
      paymentUrl: result.paymentUrl,
      status: 'CREATED',
      raw: result.raw
    });
  } catch (error) {
    console.error('Error creating ToyyibPay bill:', error.response?.data || error.message);
    res.status(400).json({
      error: 'CREATE_BILL_FAILED',
      message: error.message,
      details: error.response?.data
    });
  }
});

router.get('/payment/status', async (req, res) => {
  const { billCode } = req.query;

  if (!billCode) {
    return res.status(400).json({ error: 'Missing billCode query parameter' });
  }

  try {
    const transactions = await getBillTransactions(billCode);
    const latest = transactions.length > 0 ? transactions[transactions.length - 1] : null;

    res.json({
      billCode,
      status: latest?.status ?? 'UNKNOWN',
      paid: latest?.status === '1',
      remark: latest?.msg ?? '',
      transactions
    });
  } catch (error) {
    console.error('Error fetching ToyyibPay transactions:', error.response?.data || error.message);
    res.status(400).json({
      error: 'GET_STATUS_FAILED',
      message: error.message,
      details: error.response?.data
    });
  }
});

export default router;

