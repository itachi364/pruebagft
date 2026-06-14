import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { DashboardComponent } from './components/dashboard/dashboard.component';
import { ResultsComponent } from './components/results/results.component';
import { RulesComponent } from './components/rules/rules.component';
import { BatchProcessingResponse, MappingRule, ProcessingBatch, TransformationResult } from './models/processing.models';
import { RenamingApiService } from './services/renaming-api.service';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, DashboardComponent, ResultsComponent, RulesComponent],
  template: `
    <main class="shell">
      <header class="topbar">
        <div>
          <h1>Renombramiento S3</h1>
          <p>Operacion de reglas, procesamiento y reprocesamiento</p>
        </div>
        <div class="actions">
          <button type="button" (click)="loadFiles()">Listar</button>
          <button type="button" class="primary" [disabled]="files.length === 0" (click)="process()">Procesar</button>
        </div>
      </header>

      <section class="file-strip">
        <strong>Archivos</strong>
        <span *ngFor="let file of files">{{ file }}</span>
        <span *ngIf="files.length === 0">Sin archivos cargados</span>
      </section>

      <p class="message" *ngIf="message">{{ message }}</p>

      <app-dashboard [batch]="batch"></app-dashboard>
      <app-results [batch]="batch" [results]="results" (reprocess)="reprocess()"></app-results>
      <app-rules [rules]="rules" (refresh)="loadRules()" (toggle)="toggleRule($event)"></app-rules>
    </main>
  `
})
export class AppComponent implements OnInit {
  files: string[] = [];
  batch: ProcessingBatch | null = null;
  results: TransformationResult[] = [];
  rules: MappingRule[] = [];
  message = '';

  constructor(private readonly api: RenamingApiService) {}

  ngOnInit(): void {
    this.loadRules();
    this.loadFiles();
  }

  loadFiles(): void {
    this.api.listFiles().subscribe({
      next: response => {
        this.files = response.files;
        this.message = `Archivos disponibles: ${response.files.length}`;
      },
      error: () => this.message = 'No fue posible listar archivos.'
    });
  }

  loadRules(): void {
    this.api.listRules().subscribe({
      next: rules => this.rules = rules,
      error: () => this.message = 'No fue posible cargar reglas.'
    });
  }

  process(): void {
    this.api.process(this.files).subscribe({
      next: response => this.applyBatch(response, 'Procesamiento finalizado.'),
      error: () => this.message = 'No fue posible procesar el lote.'
    });
  }

  reprocess(): void {
    if (!this.batch) {
      return;
    }
    this.api.reprocess(this.batch.batchId).subscribe({
      next: response => this.applyBatch(response, 'Reprocesamiento finalizado.'),
      error: () => this.message = 'No fue posible reprocesar el lote.'
    });
  }

  toggleRule(rule: MappingRule): void {
    this.api.changeRuleStatus(rule.ruleId, !rule.active).subscribe({
      next: () => this.loadRules(),
      error: () => this.message = 'No fue posible cambiar el estado de la regla.'
    });
  }

  private applyBatch(response: BatchProcessingResponse, message: string): void {
    this.batch = response.batch;
    this.results = response.results;
    this.message = message;
  }
}

