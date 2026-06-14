export type ProcessingStatus = 'TRANSFORMADO' | 'ERROR' | 'NO_MAPEADO';

export interface ProcessingBatch {
  batchId: string;
  total: number;
  transformed: number;
  errors: number;
  unmapped: number;
  startedAt: string;
  finishedAt: string | null;
}

export interface TransformationResult {
  resultId: string;
  batchId: string;
  sourceFileName: string;
  targetFileName: string | null;
  status: ProcessingStatus;
  statusLabel: string;
  ruleId: string | null;
  ruleVersion: number | null;
  message: string | null;
  processedAt: string;
}

export interface BatchProcessingResponse {
  batch: ProcessingBatch;
  results: TransformationResult[];
}

export interface MappingRule {
  ruleId: string;
  version: number;
  name: string;
  sourcePattern: string;
  targetTemplate: string;
  requiresDate: boolean;
  dateStrategy: 'AUTO' | 'YYYYMMDD' | 'YYYYDDMM' | 'NONE';
  priority: number;
  active: boolean;
}

