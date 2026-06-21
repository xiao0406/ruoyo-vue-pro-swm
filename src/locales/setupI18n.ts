/**
 * Simplified i18n setup shim for SWM pages.
 * SWM pages use t('中文字符串') which just returns the key itself.
 */
import type { App } from 'vue';
import { createI18n } from 'vue-i18n';

export let i18n: ReturnType<typeof createI18n>;

export async function setupI18n(app: App) {
  i18n = createI18n({
    legacy: false,
    locale: 'zh-CN',
    fallbackLocale: 'zh-CN',
    messages: {
      'zh-CN': {},
    },
    silentTranslationWarn: true,
    missingWarn: false,
    silentFallbackWarn: true,
  }) as any;
  app.use(i18n);
}
