import { CommonModule } from '@angular/common';
import { Component, EventEmitter, Input, Output } from '@angular/core';
import { ProcessingBatch, TransformationResult } from '../../models/processing.models';

@Component({
  selector: 'app-results',
  standalone: true,
  imports: [CommonModule],
  template: `
    <section class="panel">
      <div class="panel-header">
        <h2>Resultados</h2>
        <button type="button" [disabled]="!batch" (click)="reprocess.emit()">Reprocesar</button>
      </div>
      <div class="table-wrap">
        <table>
          <thead>
            <tr>
              <th>Origen</th>
              <th>Destino</th>
              <th>Estado</th>
              <th>Regla</th>
              <th>Detalle</th>
            </tr>
          </thead>
          <tbody>
            <tr *ngFor="let result of results">
              <td>{{ result.sourceFileName }}</td>
              <td>{{ result.targetFileName || '-' }}</td>
              <td><span class="status" [class]="result.status.toLowerCase()">{{ result.statusLabel }}</span></td>
              <td>{{ result.ruleId || '-' }}</td>
              <td>{{ result.message || '-' }}</td>
            </tr>
            <tr *ngIf="results.length === 0">
              <td colspan="5" class="empty">Sin resultados</td>
            </tr>
          </tbody>
        </table>
      </div>
    </section>
  `
})
export class ResultsComponent {
  @Input() batch: ProcessingBatch | null = null;
  @Input() results: TransformationResult[] = [];
  @Output() reprocess = new EventEmitter<void>();
}

