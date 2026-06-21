type I18nGlobalTranslation = {
  (key: string): string;
  (key: string, locale: string): string;
  (key: string, locale: string, list: unknown[]): string;
  (key: string, locale: string, named: Record<string, unknown>): string;
  (key: string, list: unknown[]): string;
  (key: string, named: Record<string, unknown>): string;
};

export function useI18n(_namespace?: string): { t: I18nGlobalTranslation } {
  const t: I18nGlobalTranslation = ((key: string, ..._args: any[]) => key) as I18nGlobalTranslation;
  return { t };
}

export const t = (key: string) => key;
