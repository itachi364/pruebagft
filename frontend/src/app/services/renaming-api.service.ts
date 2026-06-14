import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { BatchProcessingResponse, MappingRule, ProcessingBatch, TransformationResult } from '../models/processing.models';

@Injectable({ providedIn: 'root' })
export class RenamingApiService {
  private readonly baseUrl = environment.apiBaseUrl;

  constructor(private readonly http: HttpClient) {}

  listFiles(): Observable<{ files: string[] }> {
    return this.http.get<{ files: string[] }>(`${this.baseUrl}/files`);
  }

  process(files: string[]): Observable<BatchProcessingResponse> {
    return this.http.post<BatchProcessingResponse>(`${this.baseUrl}/processing/batches`, { files });
  }

  reprocess(batchId: string): Observable<BatchProcessingResponse> {
    return this.http.post<BatchProcessingResponse>(`${this.baseUrl}/processing/batches/${batchId}/reprocess`, {});
  }

  getSummary(batchId: string): Observable<ProcessingBatch> {
    return this.http.get<ProcessingBatch>(`${this.baseUrl}/processing/batches/${batchId}/summary`);
  }

  getResults(batchId: string): Observable<TransformationResult[]> {
    return this.http.get<TransformationResult[]>(`${this.baseUrl}/processing/batches/${batchId}/results`);
  }

  listRules(): Observable<MappingRule[]> {
    return this.http.get<MappingRule[]>(`${this.baseUrl}/rules`);
  }

  changeRuleStatus(ruleId: string, active: boolean): Observable<MappingRule> {
    return this.http.patch<MappingRule>(`${this.baseUrl}/rules/${ruleId}/status`, { active });
  }
}

