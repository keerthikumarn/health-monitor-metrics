// Replace with your actual API endpoint and credentials
const apiUrl = '/health/summary?host=HOST_IP&user=<USERNAME>&password=<PASSWORD>';

fetch(apiUrl)
    .then(res => res.json())
    .then(data => {
        // CPU Utilization (line chart)
        const cpuRaw = data.cpu || '';
        const cpuUsage = parseFloat((cpuRaw.match(/(\d+\.\d+)\s*id/) || [])[1] || 0);
        const cpuUsed = 100 - cpuUsage;
        new Chart(document.getElementById('cpuChart'), {
            type: 'line',
            data: {
                labels: ['Now'],
                datasets: [{
                    label: 'CPU Usage (%)',
                    data: [cpuUsed],
                    borderColor: 'rgba(75,192,192,1)',
                    fill: false
                }]
            },
            options: { scales: { y: { beginAtZero: true, max: 100 } } }
        });

        // System Load (line chart)
        const loadRaw = data.systemLoad || '';
        const loadAvg = (loadRaw.match(/load average: ([\d\.]+),/) || [])[1] || 0;
        new Chart(document.getElementById('loadChart'), {
            type: 'line',
            data: {
                labels: ['Now'],
                datasets: [{
                    label: 'System Load',
                    data: [parseFloat(loadAvg)],
                    borderColor: 'rgba(255,99,132,1)',
                    fill: false
                }]
            },
            options: { scales: { y: { beginAtZero: true } } }
        });

        // System Services (pie chart)
        const servicesRaw = data.systemServices || '';
        const services = servicesRaw.split('\n').filter(line => line.trim().length > 0);
        const runningCount = services.length;
        new Chart(document.getElementById('servicesPie'), {
            type: 'pie',
            data: {
                labels: ['Running', 'Not Running'],
                datasets: [{
                    data: [runningCount, 100 - runningCount],
                    backgroundColor: ['#36a2eb', '#ffcd56']
                }]
            }
        });

        // K8S Services (table with selected columns)
        const k8sRaw = data.k8sServices || '';
        const k8sLines = k8sRaw.split('\n').filter(line => line.trim().length > 0);
        if (k8sLines.length > 0) {
            const requiredCols = ['NAME', 'CLUSTER-IP', 'PORT(S)', 'AGE'];
            const headers = k8sLines[0].split(/\s+/);
            const colIndexes = requiredCols.map(col => headers.findIndex(h => h.toUpperCase() === col));
            const headerRow = document.getElementById('k8s-header');
            requiredCols.forEach(h => {
                const th = document.createElement('th');
                th.textContent = h.replace('-', ' ').replace('PORT(S)', 'Ports');
                headerRow.appendChild(th);
            });
            const body = document.getElementById('k8s-body');
            k8sLines.slice(1).forEach(line => {
                const cells = line.split(/\s+/);
                const row = document.createElement('tr');
                colIndexes.forEach(idx => {
                    const td = document.createElement('td');
                    td.textContent = idx >= 0 ? cells[idx] : '';
                    row.appendChild(td);
                });
                body.appendChild(row);
            });
        }
    })
    .catch(err => {
        alert('Failed to load health metrics: ' + err);
    });