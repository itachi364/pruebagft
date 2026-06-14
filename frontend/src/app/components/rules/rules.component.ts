import { CommonModule } from '@angular/common';
import { Component, EventEmitter, Input, Output } from '@angular/core';
import { MappingRule } from '../../models/processing.models';

@Component({
  selector: 'app-rules',
  standalone: true,
  imports: [CommonModule],
  template: `
    <section class="panel">
      <div class="panel-header">
        <h2>Reglas</h2>
        <button type="button" (click)="refresh.emit()">Actualizar</button>
      </div>
      <div class="table-wrap">
        <table>
          <thead>
            <tr>
              <th>Prioridad</th>
              <th>Nombre</th>
              <th>Patron</th>
              <th>Plantilla</th>
              <th>Fecha</th>
              <th>Estado</th>
            </tr>
          </thead>
          <tbody>
            <tr *ngFor="let rule of rules">
              <td>{{ rule.priority }}</td>
              <td>{{ rule.name }}</td>
              <td>{{ rule.sourcePattern }}</td>
              <td>{{ rule.targetTemplate }}</td>
              <td>{{ rule.dateStrategy }}</td>
              <td>
                <button type="button" class="toggle" (click)="toggle.emit(rule)">
                  {{ rule.active ? 'Activa' : 'Inactiva' }}
                </button>
              </td>
            </tr>
            <tr *ngIf="rules.length === 0">
              <td colspan="6" class="empty">Sin reglas</td>
            </tr>
          </tbody>
        </table>
      </div>
    </section>
  `
})
export class RulesComponent {
  @Input() rules: MappingRule[] = [];
  @Output() refresh = new EventEmitter<void>();
  @Output() toggle = new EventEmitter<MappingRule>();
}

