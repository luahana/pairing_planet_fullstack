import 'package:flutter_test/flutter_test.dart';
import 'package:pairing_planet2_frontend/core/utils/locale_to_country_mapper.dart';

void main() {
  group('LocaleToCountryMapper', () {
    group('getCountryCodeFromLocaleString', () {
      group('standard underscore format (language_COUNTRY)', () {
        test('ko_KR returns KR', () {
          expect(
            LocaleToCountryMapper.getCountryCodeFromLocaleString('ko_KR'),
            'KR',
          );
        });

        test('en_US returns US', () {
          expect(
            LocaleToCountryMapper.getCountryCodeFromLocaleString('en_US'),
            'US',
          );
        });

        test('ja_JP returns JP', () {
          expect(
            LocaleToCountryMapper.getCountryCodeFromLocaleString('ja_JP'),
            'JP',
          );
        });

        test('zh_CN returns CN', () {
          expect(
            LocaleToCountryMapper.getCountryCodeFromLocaleString('zh_CN'),
            'CN',
          );
        });

        test('fr_FR returns FR', () {
          expect(
            LocaleToCountryMapper.getCountryCodeFromLocaleString('fr_FR'),
            'FR',
          );
        });

        test('de_DE returns DE', () {
          expect(
            LocaleToCountryMapper.getCountryCodeFromLocaleString('de_DE'),
            'DE',
          );
        });

        test('es_ES returns ES', () {
          expect(
            LocaleToCountryMapper.getCountryCodeFromLocaleString('es_ES'),
            'ES',
          );
        });

        test('pt_BR returns BR', () {
          expect(
            LocaleToCountryMapper.getCountryCodeFromLocaleString('pt_BR'),
            'BR',
          );
        });

        test('it_IT returns IT', () {
          expect(
            LocaleToCountryMapper.getCountryCodeFromLocaleString('it_IT'),
            'IT',
          );
        });

        test('ru_RU returns RU', () {
          expect(
            LocaleToCountryMapper.getCountryCodeFromLocaleString('ru_RU'),
            'RU',
          );
        });

        test('el_GR returns GR', () {
          expect(
            LocaleToCountryMapper.getCountryCodeFromLocaleString('el_GR'),
            'GR',
          );
        });
      });

      group('hyphen format (language-COUNTRY)', () {
        test('en-US returns US', () {
          expect(
            LocaleToCountryMapper.getCountryCodeFromLocaleString('en-US'),
            'US',
          );
        });

        test('ko-KR returns KR', () {
          expect(
            LocaleToCountryMapper.getCountryCodeFromLocaleString('ko-KR'),
            'KR',
          );
        });

        test('ja-JP returns JP', () {
          expect(
            LocaleToCountryMapper.getCountryCodeFromLocaleString('ja-JP'),
            'JP',
          );
        });

        test('fr-FR returns FR', () {
          expect(
            LocaleToCountryMapper.getCountryCodeFromLocaleString('fr-FR'),
            'FR',
          );
        });
      });

      group('script variants (language-Script_COUNTRY)', () {
        test('zh-Hans_CN returns CN', () {
          expect(
            LocaleToCountryMapper.getCountryCodeFromLocaleString('zh-Hans_CN'),
            'CN',
          );
        });

        test('zh-Hant_TW returns TW', () {
          expect(
            LocaleToCountryMapper.getCountryCodeFromLocaleString('zh-Hant_TW'),
            'TW',
          );
        });

        test('zh_Hans_CN returns CN', () {
          expect(
            LocaleToCountryMapper.getCountryCodeFromLocaleString('zh_Hans_CN'),
            'CN',
          );
        });
      });

      group('language only (inferred country)', () {
        test('ko returns KR', () {
          expect(
            LocaleToCountryMapper.getCountryCodeFromLocaleString('ko'),
            'KR',
          );
        });

        test('ja returns JP', () {
          expect(
            LocaleToCountryMapper.getCountryCodeFromLocaleString('ja'),
            'JP',
          );
        });

        test('zh returns CN', () {
          expect(
            LocaleToCountryMapper.getCountryCodeFromLocaleString('zh'),
            'CN',
          );
        });

        test('en returns US', () {
          expect(
            LocaleToCountryMapper.getCountryCodeFromLocaleString('en'),
            'US',
          );
        });

        test('fr returns FR', () {
          expect(
            LocaleToCountryMapper.getCountryCodeFromLocaleString('fr'),
            'FR',
          );
        });

        test('de returns DE', () {
          expect(
            LocaleToCountryMapper.getCountryCodeFromLocaleString('de'),
            'DE',
          );
        });

        test('es returns ES', () {
          expect(
            LocaleToCountryMapper.getCountryCodeFromLocaleString('es'),
            'ES',
          );
        });

        test('it returns IT', () {
          expect(
            LocaleToCountryMapper.getCountryCodeFromLocaleString('it'),
            'IT',
          );
        });

        test('pt returns BR', () {
          expect(
            LocaleToCountryMapper.getCountryCodeFromLocaleString('pt'),
            'BR',
          );
        });

        test('ru returns RU', () {
          expect(
            LocaleToCountryMapper.getCountryCodeFromLocaleString('ru'),
            'RU',
          );
        });

        test('el returns GR', () {
          expect(
            LocaleToCountryMapper.getCountryCodeFromLocaleString('el'),
            'GR',
          );
        });

        test('th returns TH', () {
          expect(
            LocaleToCountryMapper.getCountryCodeFromLocaleString('th'),
            'TH',
          );
        });

        test('vi returns VN', () {
          expect(
            LocaleToCountryMapper.getCountryCodeFromLocaleString('vi'),
            'VN',
          );
        });

        test('hi returns IN', () {
          expect(
            LocaleToCountryMapper.getCountryCodeFromLocaleString('hi'),
            'IN',
          );
        });

        test('ar returns SA', () {
          expect(
            LocaleToCountryMapper.getCountryCodeFromLocaleString('ar'),
            'SA',
          );
        });

        test('tr returns TR', () {
          expect(
            LocaleToCountryMapper.getCountryCodeFromLocaleString('tr'),
            'TR',
          );
        });

        test('nl returns NL', () {
          expect(
            LocaleToCountryMapper.getCountryCodeFromLocaleString('nl'),
            'NL',
          );
        });

        test('pl returns PL', () {
          expect(
            LocaleToCountryMapper.getCountryCodeFromLocaleString('pl'),
            'PL',
          );
        });

        test('sv returns SE', () {
          expect(
            LocaleToCountryMapper.getCountryCodeFromLocaleString('sv'),
            'SE',
          );
        });

        test('da returns DK', () {
          expect(
            LocaleToCountryMapper.getCountryCodeFromLocaleString('da'),
            'DK',
          );
        });

        test('fi returns FI', () {
          expect(
            LocaleToCountryMapper.getCountryCodeFromLocaleString('fi'),
            'FI',
          );
        });

        test('no returns NO', () {
          expect(
            LocaleToCountryMapper.getCountryCodeFromLocaleString('no'),
            'NO',
          );
        });

        test('id returns ID', () {
          expect(
            LocaleToCountryMapper.getCountryCodeFromLocaleString('id'),
            'ID',
          );
        });

        test('ms returns MY', () {
          expect(
            LocaleToCountryMapper.getCountryCodeFromLocaleString('ms'),
            'MY',
          );
        });
      });

      group('fallback to international', () {
        test('empty string returns international', () {
          expect(
            LocaleToCountryMapper.getCountryCodeFromLocaleString(''),
            'international',
          );
        });

        test('unknown language returns international', () {
          expect(
            LocaleToCountryMapper.getCountryCodeFromLocaleString('xyz'),
            'international',
          );
        });

        test('unknown locale format returns international', () {
          expect(
            LocaleToCountryMapper.getCountryCodeFromLocaleString('unknown_locale'),
            'international',
          );
        });
      });

      group('edge cases', () {
        test('en_GB returns GB (not US)', () {
          expect(
            LocaleToCountryMapper.getCountryCodeFromLocaleString('en_GB'),
            'GB',
          );
        });

        test('es_MX returns MX (not ES)', () {
          expect(
            LocaleToCountryMapper.getCountryCodeFromLocaleString('es_MX'),
            'MX',
          );
        });

        test('pt_PT returns PT (not BR)', () {
          expect(
            LocaleToCountryMapper.getCountryCodeFromLocaleString('pt_PT'),
            'PT',
          );
        });

        test('zh_TW returns TW (not CN)', () {
          expect(
            LocaleToCountryMapper.getCountryCodeFromLocaleString('zh_TW'),
            'TW',
          );
        });

        test('handles lowercase country code', () {
          // Country code must be uppercase to be recognized
          expect(
            LocaleToCountryMapper.getCountryCodeFromLocaleString('en_us'),
            'US', // Should infer from 'en' since 'us' is not uppercase
          );
        });
      });
    });

    group('languageToCountry map', () {
      test('contains all expected languages', () {
        expect(LocaleToCountryMapper.languageToCountry.length, 24);
      });

      test('has correct mapping for major languages', () {
        expect(LocaleToCountryMapper.languageToCountry['ko'], 'KR');
        expect(LocaleToCountryMapper.languageToCountry['ja'], 'JP');
        expect(LocaleToCountryMapper.languageToCountry['zh'], 'CN');
        expect(LocaleToCountryMapper.languageToCountry['en'], 'US');
        expect(LocaleToCountryMapper.languageToCountry['fr'], 'FR');
        expect(LocaleToCountryMapper.languageToCountry['de'], 'DE');
        expect(LocaleToCountryMapper.languageToCountry['es'], 'ES');
        expect(LocaleToCountryMapper.languageToCountry['it'], 'IT');
        expect(LocaleToCountryMapper.languageToCountry['pt'], 'BR');
        expect(LocaleToCountryMapper.languageToCountry['ru'], 'RU');
        expect(LocaleToCountryMapper.languageToCountry['el'], 'GR');
      });
    });
  });
}
