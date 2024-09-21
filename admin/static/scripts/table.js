import { tournaments } from '../data/tournaments.js'; 


let tableHTML = '';

tournaments.forEach((tournament) => {
    tableHTML += `
                <tr>
                    <td>${tournament.date}</td>
                    <td>${tournament.name}</td>
                    <td>${tournament.organizer}</td>
                    <td>${tournament.status}</td>
                    <td>${tournament.regType}</td>
                    <td>${tournament.players}</td>
                    <td>
                     <div class="edit-container">
                            <img class="edit-icon"  src="static/images/edit.png">
                        </div>
                        <div class="delete-container">
                            <img class="delete-icon"  src="static/images/delete.png">
                        </div>
                    </td>
                </tr>`;
});

document.querySelector('.content-table').innerHTML += tableHTML;
