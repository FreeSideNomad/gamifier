import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-not-found',
  imports: [CommonModule],
  template: `
    <div class="not-found lcars-panel accent-red">
      <h1>404 - Page Not Found</h1>
      <p>The requested page could not be found in the Starfleet database.</p>
      <button class="lcars-button primary" onclick="history.back()">Return to Previous Page</button>
    </div>
  `,
  styles: [`
    .not-found {
      text-align: center;
      padding: 2rem;
      margin: 2rem;
    }
  `]
})
export class NotFoundComponent {
}