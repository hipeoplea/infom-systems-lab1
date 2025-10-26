export function createWS(onMessage){
    const url = (import.meta.env.VITE_WS_URL) || `${location.protocol === 'https:' ? 'wss' : 'ws'}://${location.host}/ws`
    const ws = new WebSocket(url)
    ws.onopen = () => console.log('ws open')
    ws.onmessage = (ev) => {
        try { const msg = JSON.parse(ev.data); onMessage(msg) } catch(e){ console.warn('bad ws msg', e) }
    }
    ws.onclose = () => console.log('ws closed')
    ws.onerror = (e) => console.error(e)
    return ws
}
