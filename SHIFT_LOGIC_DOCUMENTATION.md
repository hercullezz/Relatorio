# Documentação do Sistema de Turnos e Data de Trabalho (WorkDate)

## 1. O Problema Central
Em uma fábrica que funciona 24 horas, o dia civil (do relógio) nem sempre coincide com o dia de trabalho. O caso mais crítico é o **3º Turno**, que começa em um dia e termina no outro. 

**Exemplo:**
- Um serviço feito às 22:00 de 21/03 pertence ao 3º turno de 21/03.
- Um serviço feito às 02:00 de 22/03 **também** pertence ao 3º turno de 21/03.

Se filtrarmos apenas pelo dia do relógio, o serviço das 02:00 "some" ou aparece no dia errado.

## 2. A Solução: WorkDate (Data de Trabalho)
Para resolver isso, o sistema utiliza o conceito de **WorkDate**. A WorkDate é sempre a data em que o turno **começou**.

### Regras de Cálculo (ShiftManager):
- **Turno 1 (05:00 - 13:40):** WorkDate = Dia Atual.
- **Turno 2 (13:20 - 22:00):** WorkDate = Dia Atual.
- **Turno 3 (21:30 - 05:20):**
    - Se a hora for entre 21:30 e 23:59: WorkDate = Dia Atual.
    - Se a hora for entre 00:00 e 05:20: WorkDate = **Dia Anterior**.

## 3. Implementação Técnica

### Salvamento (MainViewModel)
Ao salvar um serviço, a `workDate` é gravada no banco de dados (Parse) sempre com o horário zerado (**00:00:00**). Isso evita que diferenças de segundos ou milissegundos quebrem os filtros.

### Filtragem (UI)
A filtragem segura deve comparar as datas como **Strings** no formato `YYYY-MM-DD`. 
O uso de `ShiftManager.formatWorkDate(millis)` garante que o fuso horário local seja respeitado e que a comparação seja apenas entre os dias, ignorando as horas.

### Tolerância de 1 Hora
O sistema possui uma tolerância de 1 hora após o término oficial de cada turno para visualização e edição (conforme `getVisibleShiftInfos` no `ShiftManager`).

## 4. Guia de Solução de Problemas (Troubleshooting)

**Q: Filtrei o 3º turno do dia X e os serviços antes da meia-noite sumiram.**
- **Causa provável:** A `workDate` foi salva com horário ou fuso incorreto, ou o filtro está comparando a data do registro (`createdAt`) em vez da `workDate`.
- **Solução:** Verificar se o filtro na tela está usando `ShiftManager.formatWorkDate(item.workDate)` para comparar com a data selecionada.

**Q: O serviço aparece no PDF mas não aparece na tela.**
- **Causa provável:** O filtro da tela (`HomeScreen` ou `ServicesListScreen`) está mais restrito que a lógica do PDF.
- **Solução:** Unificar a lógica de filtro usando a função `ShiftManager.getVisibleShiftInfos()`.
