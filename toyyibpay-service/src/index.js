import 'dotenv/config';
import express from 'express';
import cors from 'cors';
import morgan from 'morgan';

import toyyibpayRouter from './routes/toyyibpay.js';

const app = express();

const port = process.env.PORT || 8080;
const allowOrigins = process.env.ALLOW_ORIGINS?.split(',').map(origin => origin.trim()).filter(Boolean) || ['*'];
const logFormat = process.env.LOG_LEVEL || 'dev';

const corsOptions = {
  origin: (origin, callback) => {
    if (!origin || allowOrigins.includes('*') || allowOrigins.includes(origin)) {
      return callback(null, true);
    }
    return callback(new Error('Not allowed by CORS'));
  }
};

app.use(cors(corsOptions));
app.use(express.urlencoded({ extended: true }));
app.use(express.json());
app.use(morgan(logFormat));

app.get('/', (_req, res) => {
  res.json({
    status: 'ok',
    service: 'MizrahBeauty ToyyibPay Service',
    docs: '/docs'
  });
});

app.use('/toyyibpay', toyyibpayRouter);

app.use((err, _req, res, _next) => {
  console.error('Unhandled error:', err);
  res.status(500).json({ error: 'Internal Server Error' });
});

app.listen(port, () => {
  console.log(`ToyyibPay service listening on port ${port}`);
});

