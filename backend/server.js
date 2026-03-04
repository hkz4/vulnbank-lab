const express    = require('express');
const bodyParser = require('body-parser');
const app        = express();

app.use(bodyParser.json());
app.use(bodyParser.urlencoded({ extended: true }));

app.use((req, res, next) => {
    console.log(`[${new Date().toISOString()}] ${req.method} ${req.url}`);
    if (req.body && Object.keys(req.body).length > 0)
        console.log('  BODY:', JSON.stringify(req.body));
    next();
});

const users = [
    { id: 1, username: 'admin', password: 'Admin@123',  balance: 99999, role: 'admin', token: 'TOKEN_ADMIN_SECRET' },
    { id: 2, username: 'alice', password: 'alice123',   balance: 5000,  role: 'user',  token: 'TOKEN_ALICE_ABC' },
    { id: 3, username: 'bob',   password: 'bob456',     balance: 3000,  role: 'user',  token: 'TOKEN_BOB_XYZ' },
];
const transactions = [];

function makeToken(payload) {
    const header = Buffer.from(JSON.stringify({ alg: 'none', typ: 'JWT' })).toString('base64url');
    const body   = Buffer.from(JSON.stringify(payload)).toString('base64url');
    return `${header}.${body}.`;
}

app.post('/api/login', (req, res) => {
    const { username, password } = req.body;
    const user = users.find(u => u.username === username && u.password === password);
    if (!user) return res.status(401).json({ error: 'Invalid credentials' });
    const token = makeToken({ userId: user.id, username: user.username, role: user.role, isAdmin: user.role === 'admin' });
    res.json({ token, userId: user.id, username: user.username, password: user.password, isAdmin: user.role === 'admin' });
});

app.get('/api/balance', (req, res) => {
    const userId = parseInt(req.query.userId);
    const user   = users.find(u => u.id === userId);
    if (!user) return res.status(404).json({ error: 'User not found' });
    res.json({ userId: user.id, username: user.username, balance: user.balance, role: user.role, token: user.token });
});

app.post('/api/transfer', (req, res) => {
    const { from, to, amount } = req.body;
    const token     = req.headers['x-token'] || req.body.token || 'no_token';
    const sender    = users.find(u => u.username === from);
    const recipient = users.find(u => u.username === to);
    if (!sender || !recipient) return res.status(400).json({ error: 'User not found' });
    const amt = parseFloat(amount);
    sender.balance    -= amt;
    recipient.balance += amt;
    const tx = { id: transactions.length + 1, from, to, amount: amt, timestamp: new Date().toISOString(), token_used: token };
    transactions.push(tx);
    res.json({ success: true, transaction: tx, sender_balance: sender.balance, recipient_balance: recipient.balance });
});

app.get('/api/users', (req, res) => { res.json({ users }); });

app.get('/api/profile', (req, res) => {
    const userId = parseInt(req.query.userId || req.query.user || 1);
    const user   = users.find(u => u.id === userId);
    if (!user) return res.status(404).json({ error: 'Not found' });
    res.json(user);
});

app.get('/api/transactions', (req, res) => { res.json({ transactions }); });

app.get('/api/debug', (req, res) => {
    res.json({
        env: 'production', version: '1.0.0',
        db_host: 'prod-db.internal:5432', db_user: 'postgres', db_password: 'Pr0d_DB_P@ss!',
        jwt_secret: 'vulnbank_jwt_secret_do_not_share',
        aws_key: 'AKIAIOSFODNN7EXAMPLE', aws_secret: 'wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY',
        internal_api: 'http://internal-api.vulnlab.local/admin',
        users: users
    });
});

app.get('/api/reset-password', (req, res) => {
    const { username, newpass } = req.query;
    const user = users.find(u => u.username === username);
    if (!user) return res.status(404).json({ error: 'User not found' });
    user.password = newpass;
    res.json({ success: true, message: `Password reset for ${username}` });
});

app.listen(3000, '0.0.0.0', () => {
    console.log('VulnBank Backend running on :3000');
    console.log('Endpoints: POST /api/login, GET /api/balance, POST /api/transfer, GET /api/users, GET /api/profile, GET /api/transactions, GET /api/debug, GET /api/reset-password');
});
