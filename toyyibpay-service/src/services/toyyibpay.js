import axios from 'axios';
import qs from 'qs';

const TOYYIBPAY_BASE_URL = process.env.TOYYIBPAY_BASE_URL || 'https://dev.toyyibpay.com/';
const TOYYIBPAY_SECRET_KEY = process.env.TOYYIBPAY_SECRET_KEY;
const TOYYIBPAY_CATEGORY_CODE = process.env.TOYYIBPAY_CATEGORY_CODE;

if (!TOYYIBPAY_SECRET_KEY) {
  console.warn('TOYYIBPAY_SECRET_KEY is not set. createBill calls will fail until configured.');
}

if (!TOYYIBPAY_CATEGORY_CODE) {
  console.warn('TOYYIBPAY_CATEGORY_CODE is not set. createBill calls will fail until configured.');
}

const toyibClient = axios.create({
  baseURL: TOYYIBPAY_BASE_URL,
  timeout: 20000
});

export async function createBill ({
  amount,
  description,
  customerName,
  customerEmail,
  customerPhone,
  referenceId,
  returnUrl,
  callbackUrl
}) {
  if (!TOYYIBPAY_SECRET_KEY || !TOYYIBPAY_CATEGORY_CODE) {
    throw new Error('ToyyibPay secret key or category code missing.');
  }

  const billAmountInCents = Math.round(Number(amount || 0) * 100);

  const payload = {
    userSecretKey: TOYYIBPAY_SECRET_KEY,
    categoryCode: TOYYIBPAY_CATEGORY_CODE,
    billName: description?.substring(0, 30) || 'Mizrah Beauty Booking',
    billDescription: description || 'Mizrah Beauty service booking',
    billPriceSetting: 1,
    billPayorInfo: 1,
    billAmount: billAmountInCents,
    billReturnUrl: returnUrl,
    billCallbackUrl: callbackUrl,
    billTo: customerName,
    billEmail: customerEmail,
    billPhone: customerPhone || '',
    billPaymentChannel: 2,
    billExternalReferenceNo: referenceId,
    enableFPXB2B: 0
  };

  const response = await toyibClient.post(
    'index.php/api/createBill',
    qs.stringify(payload),
    { headers: { 'Content-Type': 'application/x-www-form-urlencoded' } }
  );

  console.log('ToyyibPay raw response:', JSON.stringify(response.data));

  let result = response.data;
  
  // ToyyibPay sometimes returns [{"BillCode":"..."}] or just {"BillCode":"..."}
  if (Array.isArray(result) && result.length > 0) {
    result = result[0];
  }

  if (!result || !result.BillCode) {
    console.error('ToyyibPay createBill missing BillCode. Raw response:', response.data);
    throw new Error('Failed to receive BillCode from ToyyibPay');
  }

  return {
    billCode: result.BillCode,
    paymentUrl: `${TOYYIBPAY_BASE_URL}${result.BillCode}`,
    raw: result
  };
}

export async function getBillTransactions (billCode) {
  if (!TOYYIBPAY_SECRET_KEY) {
    throw new Error('ToyyibPay secret key missing.');
  }

  const payload = {
    userSecretKey: TOYYIBPAY_SECRET_KEY,
    billCode
  };

  console.log('Fetching transactions for billCode:', billCode);

  const response = await toyibClient.post(
    'index.php/api/getBillTransactions',
    qs.stringify(payload),
    { headers: { 'Content-Type': 'application/x-www-form-urlencoded' } }
  );

  console.log('ToyyibPay transactions response:', JSON.stringify(response.data));

  return Array.isArray(response.data) ? response.data : [];
}

