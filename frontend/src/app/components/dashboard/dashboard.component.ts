import { CommonModule } from '@angular/common';
import { Component, Input } from '@angular/core';
import { ProcessingBatch } from '../../models/processing.models';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule],
  template: `
    <section class="summary-grid" aria-label="Resumen">
      <div class="metric">
        <span>Total</span>
        <strong>{{ batch?.total ?? 0 }}</strong>
      </div>
      <div class="metric ok">
        <span>Transformados</span>
        <strong>{{ batch?.transformed ?? 0 }}</strong>
      </div>
      <div class="metric warn">
        <span>No mapeados</span>
        <strong>{{ batch?.unmapped ?? 0 }}</strong>
      </div>
      <div class="metric danger">
        <span>Errores</span>
        <strong>{{ batch?.errors ?? 0 }}</strong>
      </div>
    </section>
  `
})
export class DashboardComponent {
  @Input() batch: ProcessingBatch | null = null;
}

