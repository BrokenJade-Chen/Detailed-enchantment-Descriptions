// 选择性代理：只有 neoforged 走梯子，其它（Mojang/Fabric）直连。
// 用法: node selective-proxy.mjs  然后 Gradle 用 -Dhttps.proxyPort=18080
import http from 'node:http';
import net from 'node:net';

const LISTEN = 18080;
const UPSTREAM = { host: '127.0.0.1', port: 33210 };
const viaProxy = (host) => host.endsWith('neoforged.net') || host.endsWith('neoforged.com');

http.createServer((req, res) => {
  res.writeHead(400);
  res.end('CONNECT only');
}).on('connect', (req, client, head) => {
  const [host, rawPort] = req.url.split(':');
  const port = Number(rawPort) || 443;
  client.on('error', () => {});

  if (viaProxy(host)) {
    const up = net.connect(UPSTREAM.port, UPSTREAM.host, () => {
      up.write(`CONNECT ${host}:${port} HTTP/1.1\r\nHost: ${host}:${port}\r\n\r\n`);
    });
    up.on('error', () => client.destroy());
    up.once('data', (chunk) => {
      if (!chunk.toString('latin1').startsWith('HTTP/1.1 200')) {
        client.destroy();
        up.destroy();
        return;
      }
      client.write('HTTP/1.1 200 Connection Established\r\n\r\n');
      if (head && head.length) client.write(head);
      up.pipe(client);
      client.pipe(up);
    });
    client.on('close', () => up.destroy());
  } else {
    const srv = net.connect(port, host, () => {
      client.write('HTTP/1.1 200 Connection Established\r\n\r\n');
      if (head && head.length) client.write(head);
      srv.pipe(client);
      client.pipe(srv);
    });
    srv.on('error', () => client.destroy());
    client.on('close', () => srv.destroy());
  }
}).listen(LISTEN, '127.0.0.1', () => console.log('selective proxy on', LISTEN));
