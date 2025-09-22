const fs = require('fs');

async function checkExistingData() {
    console.log('🔍 Checking existing tasks and projects...');
    
    try {
        const token = fs.readFileSync('manager_token_fresh.txt', 'utf8').trim();
        
        // 检查存在的任务
        console.log('\n📋 Checking existing tasks...');
        const tasksResponse = await fetch('http://localhost:8081/api/simple/tasks', {
            method: 'GET',
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });
        
        if (tasksResponse.status === 200) {
            const tasksResult = await tasksResponse.json();
            console.log('Tasks response:', JSON.stringify(tasksResult, null, 2));
        } else {
            console.log('Tasks request failed:', tasksResponse.status);
        }
        
        // 检查存在的项目
        console.log('\n🏗️ Checking existing projects...');
        const projectsResponse = await fetch('http://localhost:8081/api/simple/projects', {
            method: 'GET',
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });
        
        if (projectsResponse.status === 200) {
            const projectsResult = await projectsResponse.json();
            console.log('Projects response:', JSON.stringify(projectsResult, null, 2));
            
            // 查找项目阶段
            if (projectsResult.success && projectsResult.data && projectsResult.data.length > 0) {
                const project = projectsResult.data[0];
                console.log(`\n📊 Checking phases for project ID: ${project.id}...`);
                
                const phasesResponse = await fetch(`http://localhost:8081/api/simple/projects/${project.id}/phases`, {
                    method: 'GET',
                    headers: {
                        'Authorization': `Bearer ${token}`
                    }
                });
                
                if (phasesResponse.status === 200) {
                    const phasesResult = await phasesResponse.json();
                    console.log('Phases response:', JSON.stringify(phasesResult, null, 2));
                } else {
                    console.log('Phases request failed:', phasesResponse.status);
                }
            }
        } else {
            console.log('Projects request failed:', projectsResponse.status);
        }

    } catch (error) {
        console.error('❌ Error:', error.message);
    }
}

checkExistingData();