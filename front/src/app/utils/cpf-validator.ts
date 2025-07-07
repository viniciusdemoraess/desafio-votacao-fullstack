export class CpfValidator {
  static validate(cpf: string): boolean {
    if (!cpf) return false;
    
    // Remove formatação
    cpf = cpf.replace(/\D/g, '');
    
    // Verifica se tem 11 dígitos
    if (cpf.length !== 11) return false;
    
    // Verifica se todos os dígitos são iguais
    if (/^(\d)\1{10}$/.test(cpf)) return false;
    
    // Validação do primeiro dígito verificador
    let sum = 0;
    for (let i = 0; i < 9; i++) {
      sum += parseInt(cpf.charAt(i)) * (10 - i);
    }
    let remainder = 11 - (sum % 11);
    let digit1 = remainder < 2 ? 0 : remainder;
    
    if (parseInt(cpf.charAt(9)) !== digit1) return false;
    
    // Validação do segundo dígito verificador
    sum = 0;
    for (let i = 0; i < 10; i++) {
      sum += parseInt(cpf.charAt(i)) * (11 - i);
    }
    remainder = 11 - (sum % 11);
    let digit2 = remainder < 2 ? 0 : remainder;
    
    return parseInt(cpf.charAt(10)) === digit2;
  }
  
  static format(cpf: string): string {
    cpf = cpf.replace(/\D/g, '');
    return cpf.replace(/(\d{3})(\d{3})(\d{3})(\d{2})/, '$1.$2.$3-$4');
  }
  
  static generate(): string {
    // Gera os 9 primeiros dígitos aleatoriamente
    const digits = [];
    for (let i = 0; i < 9; i++) {
      digits.push(Math.floor(Math.random() * 9));
    }
    
    // Calcula o primeiro dígito verificador
    let sum = 0;
    for (let i = 0; i < 9; i++) {
      sum += digits[i] * (10 - i);
    }
    let remainder = 11 - (sum % 11);
    const digit1 = remainder < 2 ? 0 : remainder;
    digits.push(digit1);
    
    // Calcula o segundo dígito verificador
    sum = 0;
    for (let i = 0; i < 10; i++) {
      sum += digits[i] * (11 - i);
    }
    remainder = 11 - (sum % 11);
    const digit2 = remainder < 2 ? 0 : remainder;
    digits.push(digit2);
    
    return digits.join('');
  }
}

// Função auxiliar para facilitar o uso
export function generateValidCpf(): string {
  return CpfValidator.generate();
}
