import { of } from 'rxjs';
import { AppComponent } from './app.component';
import { RenamingApiService } from './services/renaming-api.service';

describe('AppComponent', () => {
  it('loads files and rules on init', () => {
    const api = jasmine.createSpyObj<RenamingApiService>('RenamingApiService', [
      'listFiles',
      'listRules',
      'process',
      'reprocess',
      'changeRuleStatus'
    ]);
    api.listFiles.and.returnValue(of({ files: ['PHO_CD_DES_20260430'] }));
    api.listRules.and.returnValue(of([]));

    const component = new AppComponent(api);
    component.ngOnInit();

    expect(component.files).toEqual(['PHO_CD_DES_20260430']);
    expect(component.rules).toEqual([]);
  });

  it('applies batch response after processing', () => {
    const api = jasmine.createSpyObj<RenamingApiService>('RenamingApiService', [
      'listFiles',
      'listRules',
      'process',
      'reprocess',
      'changeRuleStatus'
    ]);
    api.process.and.returnValue(of({
      batch: {
        batchId: 'batch-1',
        total: 1,
        transformed: 1,
        errors: 0,
        unmapped: 0,
        startedAt: '2026-01-01T00:00:00Z',
        finishedAt: '2026-01-01T00:00:01Z'
      },
      results: []
    }));

    const component = new AppComponent(api);
    component.files = ['PHO_CD_DES_20260430'];
    component.process();

    expect(component.batch?.batchId).toBe('batch-1');
    expect(component.message).toBe('Procesamiento finalizado.');
  });
});

